const API = {
    cashback: '/api/rules/cashback',
    services: '/api/rules/services',
    goals: '/api/rules/goals',
    config: '/api/rules/config'
};

const currentTab = 'cashback';

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.addEventListener('click', () => switchTab(btn.dataset.tab));
    });
    document.getElementById('rule-form').addEventListener('submit', submitForm);
    loadAll();
});

function switchTab(tab) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.toggle('active', b.dataset.tab === tab));
    document.querySelectorAll('.tab-content').forEach(c => c.classList.toggle('active', c.id === 'tab-' + tab));
    loadTab(tab);
}

function loadAll() {
    loadTab('cashback');
    loadTab('services');
    loadTab('goals');
    loadTab('config');
}

function loadTab(tab) {
    if (tab === 'cashback') loadCashback();
    else if (tab === 'services') loadServices();
    else if (tab === 'goals') loadGoals();
    else loadConfig();
}

async function fetchJson(url, options) {
    const res = await fetch(url, options);
    const data = await res.json().catch(() => null);
    if (!res.ok) {
        const msg = data && data.message ? data.message : 'Request failed (' + res.status + ')';
        throw new Error(msg);
    }
    return data;
}

function tagHtml(rewardType) {
    const type = (rewardType || 'CASHBACK').toUpperCase();
    return `<span class="tag tag-${type.toLowerCase()}">${type}</span>`;
}

function fmt(value) {
    return value === null || value === undefined || value === '' ? '—' : value;
}

/* ---------- Cashback ---------- */

async function loadCashback() {
    try {
        const rules = await fetchJson(API.cashback);
        const tbody = document.getElementById('cashback-body');
        if (rules.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty">No cashback rules yet</td></tr>';
            return;
        }
        tbody.innerHTML = rules.map(r => `
            <tr>
                <td>${r.id}</td>
                <td>₹${r.minAmount}</td>
                <td>${r.maxAmount === null ? '∞' : '₹' + r.maxAmount}</td>
                <td>₹${r.cashbackAmount}</td>
                <td>${tagHtml(r.rewardType)}</td>
                <td>${fmt(r.rewardValue)}</td>
                <td>
                    <button class="btn btn-edit" onclick="editRule('cashback', ${r.id})">Edit</button>
                    <button class="btn btn-delete" onclick="deleteRule('cashback', ${r.id})">Delete</button>
                </td>
            </tr>`).join('');
    } catch (err) {
        toast('Failed to load cashback rules: ' + err.message, true);
    }
}

/* ---------- Services ---------- */

async function loadServices() {
    try {
        const rules = await fetchJson(API.services);
        const tbody = document.getElementById('services-body');
        if (rules.length === 0) {
            tbody.innerHTML = '<tr><td colspan="7" class="empty">No service rules yet</td></tr>';
            return;
        }
        tbody.innerHTML = rules.map(r => `
            <tr>
                <td>${r.id}</td>
                <td><strong>${r.serviceType}</strong></td>
                <td>${r.percentage}%</td>
                <td>₹${r.maxCap}</td>
                <td>${tagHtml(r.rewardType)}</td>
                <td>${fmt(r.rewardValue)}</td>
                <td>
                    <button class="btn btn-edit" onclick="editRule('services', ${r.id})">Edit</button>
                    <button class="btn btn-delete" onclick="deleteRule('services', ${r.id})">Delete</button>
                </td>
            </tr>`).join('');
    } catch (err) {
        toast('Failed to load service rules: ' + err.message, true);
    }
}

/* ---------- Goals ---------- */

async function loadGoals() {
    try {
        const rules = await fetchJson(API.goals);
        const tbody = document.getElementById('goals-body');
        if (rules.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="empty">No goal rules yet</td></tr>';
            return;
        }
        tbody.innerHTML = rules.map(r => `
            <tr>
                <td>${r.id}</td>
                <td>${r.transactionCount} transactions</td>
                <td>₹${r.reward}</td>
                <td>${tagHtml(r.rewardType)}</td>
                <td>${fmt(r.rewardValue)}</td>
                <td>
                    <button class="btn btn-edit" onclick="editRule('goals', ${r.id})">Edit</button>
                    <button class="btn btn-delete" onclick="deleteRule('goals', ${r.id})">Delete</button>
                </td>
            </tr>`).join('');
    } catch (err) {
        toast('Failed to load goal rules: ' + err.message, true);
    }
}

/* ---------- Config ---------- */

async function loadConfig() {
    try {
        const configs = await fetchJson(API.config);
        const tbody = document.getElementById('config-body');
        if (configs.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="empty">No config values</td></tr>';
            return;
        }
        tbody.innerHTML = configs.map(c => `
            <tr>
                <td>${c.id}</td>
                <td><code>${c.configKey}</code></td>
                <td id="cfg-val-${c.id}">${c.configValue}</td>
                <td>
                    <button class="btn btn-edit" onclick="editConfig(${c.id}, '${c.configKey}')">Edit</button>
                </td>
            </tr>`).join('');
    } catch (err) {
        toast('Failed to load config: ' + err.message, true);
    }
}

async function editConfig(id, key) {
    const current = document.getElementById('cfg-val-' + id).textContent;
    const value = prompt('New value for ' + key + ':', current);
    if (value === null) return;
    try {
        const updated = await fetchJson(API.config + '/' + encodeURIComponent(key), {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ configValue: value })
        });
        document.getElementById('cfg-val-' + id).textContent = updated.configValue;
        toast('Config updated: ' + key + ' = ' + updated.configValue);
    } catch (err) {
        toast(err.message, true);
    }
}

/* ---------- Modal form ---------- */

const FIELD_DEFS = {
    cashback: [
        { key: 'minAmount', label: 'Min Amount (₹)', type: 'number', step: 'any', required: true },
        { key: 'maxAmount', label: 'Max Amount (₹) — leave empty for unlimited', type: 'number', step: 'any' },
        { key: 'cashbackAmount', label: 'Cashback (₹)', type: 'number', step: 'any', required: true }
    ],
    services: [
        { key: 'serviceType', label: 'Service Type', type: 'text', placeholder: 'e.g. GAS', required: true, upper: true },
        { key: 'percentage', label: 'Percentage (%)', type: 'number', step: 'any', required: true },
        { key: 'maxCap', label: 'Max Cap (₹)', type: 'number', step: 'any', required: true }
    ],
    goals: [
        { key: 'transactionCount', label: 'Transaction Count', type: 'number', required: true },
        { key: 'reward', label: 'Reward Amount (₹)', type: 'number', step: 'any', required: true }
    ]
};

let currentFormTab = 'cashback';
let editingId = null;

function openForm(tab) {
    currentFormTab = tab;
    editingId = null;
    document.getElementById('modal-title').textContent = 'Add ' + tab.slice(0, -1) + ' Rule';
    buildForm({});
    document.getElementById('modal').classList.remove('hidden');
}

function editRule(tab, id) {
    currentFormTab = tab;
    editingId = id;
    fetchJson(API[tab]).then(rules => {
        const rule = rules.find(r => r.id === id);
        if (!rule) return;
        document.getElementById('modal-title').textContent = 'Edit ' + tab.slice(0, -1) + ' Rule';
        buildForm(rule);
        document.getElementById('modal').classList.remove('hidden');
    }).catch(err => toast(err.message, true));
}

function buildForm(rule) {
    const fields = document.getElementById('form-fields');
    let html = '';
    for (const def of FIELD_DEFS[currentFormTab]) {
        let value = rule[def.key] ?? '';
        if (def.type === 'number' && value === null) value = '';
        html += `
            <div class="form-group">
                <label>${def.label}${def.required ? ' *' : ''}</label>
                <input type="${def.type}" step="${def.step || ''}" id="f-${def.key}"
                       value="${value}" ${def.required ? 'required' : ''}
                       placeholder="${def.placeholder || ''}">
            </div>`;
    }
    html += `
        <div class="form-group">
            <label>Reward Type *</label>
            <select id="f-rewardType" onchange="toggleRewardValue()">
                <option value="CASHBACK">CASHBACK (money credited)</option>
                <option value="COUPON">COUPON (no money)</option>
                <option value="SUBSCRIPTION">SUBSCRIPTION (no money)</option>
            </select>
        </div>
        <div class="form-group" id="reward-value-group">
            <label>Reward Value (coupon code / subscription name)</label>
            <input type="text" id="f-rewardValue" placeholder="e.g. COUPON-AMZ100">
        </div>`;
    fields.innerHTML = html;
    if (rule.rewardType) document.getElementById('f-rewardType').value = rule.rewardType;
    document.getElementById('f-rewardValue').value = rule.rewardValue || '';
    toggleRewardValue();
}

function toggleRewardValue() {
    const type = document.getElementById('f-rewardType').value;
    document.getElementById('reward-value-group').style.display =
        type === 'CASHBACK' ? 'none' : 'block';
}

function closeForm() {
    document.getElementById('modal').classList.add('hidden');
}

function collectForm() {
    const data = {};
    for (const def of FIELD_DEFS[currentFormTab]) {
        const raw = document.getElementById('f-' + def.key).value.trim();
        if (def.type === 'number') {
            data[def.key] = raw === '' ? null : parseFloat(raw);
        } else {
            data[def.key] = def.upper ? raw.toUpperCase() : raw;
        }
    }
    data.rewardType = document.getElementById('f-rewardType').value;
    data.rewardValue = document.getElementById('f-rewardValue').value.trim() || null;
    return data;
}

async function submitForm(e) {
    e.preventDefault();
    const data = collectForm();
    const url = API[currentFormTab] + (editingId ? '/' + editingId : '');
    const method = editingId ? 'PUT' : 'POST';
    try {
        await fetchJson(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        closeForm();
        loadTab(currentFormTab);
        toast(editingId ? 'Rule updated' : 'Rule added');
    } catch (err) {
        toast(err.message, true);
    }
}

/* ---------- Delete ---------- */

async function deleteRule(tab, id) {
    if (!confirm('Delete this rule? Changes apply immediately.')) return;
    try {
        await fetchJson(API[tab] + '/' + id, { method: 'DELETE' });
        loadTab(tab);
        toast('Rule deleted');
    } catch (err) {
        toast(err.message, true);
    }
}

/* ---------- Toast ---------- */

let toastTimer = null;

function toast(message, isError) {
    const el = document.getElementById('toast');
    el.textContent = message;
    el.classList.toggle('error', !!isError);
    el.classList.remove('hidden');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.add('hidden'), 3000);
}