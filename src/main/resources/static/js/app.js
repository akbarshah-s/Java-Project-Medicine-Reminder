let state = {
    medicines: [],
    dueMedicines: [],
    dismissedReminderIds: new Set(),
    activeReminder: null,
    currentSection: 'dashboard',
    editingId: null,
    deletingId: null,
    searchTimeout: null
};

const API_BASE_URL = '/api/medicines';

document.addEventListener('DOMContentLoaded', () => {
    initApp();
});

function initApp() {
    startLiveClock();
    setupNavigation();
    setupForms();
    setupSearch();
    setupModals();
    
    loadMedicines();
    
    setInterval(pollDueMedicines, 10000);
}

function startLiveClock() {
    updateClockDisplay();
    setInterval(updateClockDisplay, 1000);
}

function updateClockDisplay() {
    const clockElement = document.getElementById('clock-display');
    if (clockElement) {
        const now = new Date();
        clockElement.textContent = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: true });
    }
}

function formatTime12Hr(timeStr) {
    if (!timeStr) return '--:--';
    const parts = timeStr.split(':');
    let hours = parseInt(parts[0], 10);
    const minutes = parts[1];
    const ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12;
    hours = hours ? hours : 12;
    return `${hours < 10 ? '0' + hours : hours}:${minutes} ${ampm}`;
}

function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const section = item.getAttribute('data-section');
            switchSection(section);
        });
    });

    const mobileBtn = document.getElementById('mobile-toggle');
    const sidebar = document.getElementById('sidebar');
    if (mobileBtn && sidebar) {
        mobileBtn.addEventListener('click', () => {
            sidebar.classList.toggle('mobile-open');
        });
    }
}

function switchSection(sectionId) {
    state.currentSection = sectionId;

    document.querySelectorAll('.nav-item').forEach(item => {
        if (item.getAttribute('data-section') === sectionId) {
            item.classList.add('active');
        } else {
            item.classList.remove('active');
        }
    });

    document.querySelectorAll('.content-section').forEach(sec => {
        sec.classList.remove('active');
    });

    const targetSection = document.getElementById(`section-${sectionId}`);
    if (targetSection) {
        targetSection.classList.add('active');
    }

    const pageHeading = document.getElementById('page-heading');
    if (pageHeading) {
        const titles = {
            'dashboard': 'Dashboard Overview',
            'add-medicine': 'Add New Medicine',
            'medicines': 'All Registered Medicines',
            'schedule': "Today's Medicine Schedule",
            'upcoming': 'Upcoming Reminders'
        };
        pageHeading.textContent = titles[sectionId] || 'Medicine Reminder';
    }

    const sidebar = document.getElementById('sidebar');
    if (sidebar) sidebar.classList.remove('mobile-open');

    renderCurrentSection();
}

async function loadMedicines() {
    try {
        const response = await fetch(API_BASE_URL);
        if (!response.ok) throw new Error('Failed to load medicines');
        const data = await response.json();
        state.medicines = data;
        renderAllViews();
        pollDueMedicines();
    } catch (error) {
        console.error('API Error:', error);
        showToast('Error connecting to backend Java service', 'danger');
    }
}

async function searchMedicinesApi(query) {
    try {
        const response = await fetch(`${API_BASE_URL}/search?name=${encodeURIComponent(query)}`);
        if (!response.ok) throw new Error('Search failed');
        const data = await response.json();
        renderMedicinesTable(data);
    } catch (error) {
        console.error('Search error:', error);
    }
}

async function pollDueMedicines() {
    try {
        const response = await fetch(`${API_BASE_URL}/due`);
        if (!response.ok) return;
        const dueList = await response.json();
        state.dueMedicines = dueList;

        for (const med of dueList) {
            if (!state.dismissedReminderIds.has(med.id) && !med.isTakenToday) {
                triggerReminderAlert(med);
                break;
            }
        }
        renderDashboardStats();
    } catch (error) {
        console.warn('Background due check failed:', error);
    }
}

function renderAllViews() {
    renderDashboardStats();
    renderDashboardSchedule();
    renderMedicinesTable(state.medicines);
    renderTodaySchedule();
    renderUpcomingSchedule();
}

function renderCurrentSection() {
    switch (state.currentSection) {
        case 'dashboard':
            renderDashboardStats();
            renderDashboardSchedule();
            break;
        case 'medicines':
            renderMedicinesTable(state.medicines);
            break;
        case 'schedule':
            renderTodaySchedule();
            break;
        case 'upcoming':
            renderUpcomingSchedule();
            break;
    }
}

function renderDashboardStats() {
    const totalCount = state.medicines.length;
    const takenCount = state.medicines.filter(m => m.isTakenToday).length;
    
    document.getElementById('stat-total-medicines').textContent = totalCount;
    document.getElementById('stat-today-medicines').textContent = totalCount;
    document.getElementById('stat-taken-count').textContent = takenCount;

    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();

    let upcomingMeds = state.medicines.filter(m => {
        const [h, min] = m.time.split(':').map(Number);
        const medMinutes = h * 60 + min;
        return medMinutes >= currentMinutes && !m.isTakenToday;
    });

    if (upcomingMeds.length > 0) {
        upcomingMeds.sort((a, b) => a.time.localeCompare(b.time));
        const nextMed = upcomingMeds[0];
        document.getElementById('stat-next-reminder-time').textContent = formatTime12Hr(nextMed.time);
        document.getElementById('stat-next-reminder-name').textContent = `Next: ${nextMed.medicineName}`;
    } else {
        document.getElementById('stat-next-reminder-time').textContent = '--:--';
        document.getElementById('stat-next-reminder-name').textContent = 'No upcoming reminders';
    }
}

function renderDashboardSchedule() {
    const container = document.getElementById('dashboard-schedule-container');
    if (!container) return;

    if (state.medicines.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fa-solid fa-calendar-minus"></i>
                <p>No medicines scheduled for today.</p>
                <button class="btn btn-sm btn-primary" onclick="switchSection('add-medicine')">+ Add First Medicine</button>
            </div>
        `;
        return;
    }

    const sorted = [...state.medicines].sort((a, b) => a.time.localeCompare(b.time));
    let html = '';

    sorted.forEach(m => {
        const isTaken = m.isTakenToday;
        const statusClass = isTaken ? 'taken' : '';
        const badgeClass = isTaken ? 'badge-success' : 'badge-primary';
        const badgeText = isTaken ? '✓ Taken' : 'Pending';

        html += `
            <div class="schedule-item-card ${statusClass}">
                <div class="time-badge">${formatTime12Hr(m.time)}</div>
                <div class="med-info">
                    <h4>${escapeHtml(m.medicineName)}</h4>
                    <p>${escapeHtml(m.dosage)} &bull; ${escapeHtml(m.frequency)}</p>
                </div>
                <div>
                    <span class="badge ${badgeClass}">${badgeText}</span>
                    ${!isTaken ? `<button class="btn btn-sm btn-success" onclick="handleMarkAsTaken(${m.id})">Mark Taken</button>` : ''}
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

function renderMedicinesTable(medicinesList) {
    const tbody = document.getElementById('medicines-table-body');
    const emptyState = document.getElementById('medicines-empty-state');
    const table = document.getElementById('medicines-table');

    if (!tbody) return;

    if (!medicinesList || medicinesList.length === 0) {
        tbody.innerHTML = '';
        table.classList.add('hidden');
        emptyState.classList.remove('hidden');
        return;
    }

    table.classList.remove('hidden');
    emptyState.classList.add('hidden');

    let html = '';
    medicinesList.forEach(m => {
        const isTaken = m.isTakenToday;
        const statusBadge = isTaken 
            ? `<span class="badge badge-success"><i class="fa-solid fa-check"></i> Taken</span>` 
            : `<span class="badge badge-warning"><i class="fa-solid fa-clock"></i> Scheduled</span>`;

        html += `
            <tr>
                <td><strong>${escapeHtml(m.medicineName)}</strong></td>
                <td>${escapeHtml(m.dosage)}</td>
                <td><span class="badge badge-neutral">${formatTime12Hr(m.time)}</span></td>
                <td>${escapeHtml(m.frequency)}</td>
                <td>${statusBadge}</td>
                <td class="text-right">
                    <button class="btn btn-sm btn-outline" onclick="openEditModal(${m.id})">
                        <i class="fa-solid fa-pen"></i> Edit
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="openDeleteModal(${m.id}, '${escapeJsString(m.medicineName)}')">
                        <i class="fa-solid fa-trash"></i> Delete
                    </button>
                </td>
            </tr>
        `;
    });

    tbody.innerHTML = html;
}

function renderTodaySchedule() {
    const container = document.getElementById('today-schedule-list');
    if (!container) return;

    if (state.medicines.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <i class="fa-solid fa-calendar-xmark"></i>
                <h3>No medicines scheduled for today</h3>
                <p>Add a medicine to build your schedule.</p>
            </div>
        `;
        return;
    }

    const sorted = [...state.medicines].sort((a, b) => a.time.localeCompare(b.time));
    let html = '';

    sorted.forEach(m => {
        const isTaken = m.isTakenToday;
        html += `
            <div class="schedule-item-card ${isTaken ? 'taken' : ''}">
                <div class="time-badge">${formatTime12Hr(m.time)}</div>
                <div class="med-info">
                    <h4>${escapeHtml(m.medicineName)}</h4>
                    <p>Dosage: ${escapeHtml(m.dosage)} | ${escapeHtml(m.frequency)}</p>
                </div>
                <div>
                    ${isTaken 
                        ? `<span class="badge badge-success"><i class="fa-solid fa-check"></i> Taken</span>` 
                        : `<button class="btn btn-sm btn-success" onclick="handleMarkAsTaken(${m.id})"><i class="fa-solid fa-check"></i> Mark as Taken</button>`
                    }
                </div>
            </div>
        `;
    });

    container.innerHTML = html;
}

function renderUpcomingSchedule() {
    const container = document.getElementById('upcoming-schedule-list');
    if (!container) return;

    const now = new Date();
    const currentMinutes = now.getHours() * 60 + now.getMinutes();

    let upcoming = state.medicines.filter(m => {
        const [h, min] = m.time.split(':').map(Number);
        return (h * 60 + min) >= currentMinutes && !m.isTakenToday;
    });

    if (upcoming.length === 0) {
        container.innerHTML = `
            <div class="empty-state grid-col-span-full">
                <i class="fa-solid fa-circle-check" style="color: var(--success-color);"></i>
                <h3>All clear for today!</h3>
                <p>You have no pending upcoming medicines scheduled for today.</p>
            </div>
        `;
        return;
    }

    upcoming.sort((a, b) => a.time.localeCompare(b.time));
    let html = '';

    upcoming.forEach(m => {
        html += `
            <div class="upcoming-card">
                <div class="header">
                    <span class="badge badge-primary">${formatTime12Hr(m.time)}</span>
                    <span class="badge badge-neutral">${escapeHtml(m.frequency)}</span>
                </div>
                <h3>${escapeHtml(m.medicineName)}</h3>
                <p class="text-muted">Dosage: ${escapeHtml(m.dosage)}</p>
                <button class="btn btn-sm btn-success btn-block" onclick="handleMarkAsTaken(${m.id})">
                    <i class="fa-solid fa-check"></i> Mark as Taken
                </button>
            </div>
        `;
    });

    container.innerHTML = html;
}

function setupForms() {
    const addForm = document.getElementById('add-medicine-form');
    if (addForm) {
        addForm.addEventListener('submit', handleAddMedicineSubmit);
    }

    const editForm = document.getElementById('edit-medicine-form');
    if (editForm) {
        editForm.addEventListener('submit', handleEditMedicineSubmit);
    }

    const cancelAddBtn = document.getElementById('btn-cancel-add');
    if (cancelAddBtn) {
        cancelAddBtn.addEventListener('click', () => {
            addForm.reset();
            clearValidationErrors();
            switchSection('dashboard');
        });
    }
}

function clearValidationErrors() {
    document.querySelectorAll('.error-text').forEach(el => el.textContent = '');
}

async function handleAddMedicineSubmit(e) {
    e.preventDefault();
    clearValidationErrors();

    const name = document.getElementById('medicineName').value.trim();
    const dosage = document.getElementById('dosage').value.trim();
    const time = document.getElementById('time').value;
    const frequency = document.getElementById('frequency').value;

    let isValid = true;

    if (!name) {
        document.getElementById('err-medicineName').textContent = 'Please enter a medicine name.';
        isValid = false;
    }
    if (!dosage) {
        document.getElementById('err-dosage').textContent = 'Please enter dosage.';
        isValid = false;
    }
    if (!time) {
        document.getElementById('err-time').textContent = 'Please select a valid time.';
        isValid = false;
    }
    if (!frequency) {
        document.getElementById('err-frequency').textContent = 'Please select a frequency.';
        isValid = false;
    }

    if (!isValid) return;

    const formattedTime = time.length === 5 ? `${time}:00` : time;

    const payload = {
        medicineName: name,
        dosage: dosage,
        time: formattedTime,
        frequency: frequency
    };

    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errData = await response.json();
            throw new Error(errData.message || 'Failed to add medicine');
        }

        const newMed = await response.json();
        showToast('Medicine added successfully.', 'success');
        document.getElementById('add-medicine-form').reset();
        await loadMedicines();
        switchSection('medicines');
    } catch (error) {
        console.error('Add Medicine Error:', error);
        showToast(error.message || 'Failed to create medicine', 'danger');
    }
}

async function handleEditMedicineSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('edit-id').value;
    const name = document.getElementById('edit-medicineName').value.trim();
    const dosage = document.getElementById('edit-dosage').value.trim();
    const time = document.getElementById('edit-time').value;
    const frequency = document.getElementById('edit-frequency').value;

    const formattedTime = time.length === 5 ? `${time}:00` : time;

    const payload = {
        medicineName: name,
        dosage: dosage,
        time: formattedTime,
        frequency: frequency
    };

    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error('Failed to update medicine');

        showToast('Medicine updated successfully.', 'success');
        closeModal('edit-modal');
        await loadMedicines();
    } catch (error) {
        console.error('Edit Error:', error);
        showToast('Failed to update medicine record', 'danger');
    }
}

function setupSearch() {
    const searchInput = document.getElementById('search-input');
    const clearBtn = document.getElementById('btn-clear-search');

    if (!searchInput) return;

    searchInput.addEventListener('input', (e) => {
        const query = e.target.value.trim();
        if (query.length > 0) {
            clearBtn.style.display = 'block';
        } else {
            clearBtn.style.display = 'none';
        }

        clearTimeout(state.searchTimeout);
        state.searchTimeout = setTimeout(() => {
            searchMedicinesApi(query);
        }, 300);
    });

    clearBtn.addEventListener('click', () => {
        searchInput.value = '';
        clearBtn.style.display = 'none';
        renderMedicinesTable(state.medicines);
    });
}

function setupModals() {
    document.getElementById('btn-close-edit')?.addEventListener('click', () => closeModal('edit-modal'));
    document.getElementById('btn-cancel-edit')?.addEventListener('click', () => closeModal('edit-modal'));

    document.getElementById('btn-close-delete')?.addEventListener('click', () => closeModal('delete-modal'));
    document.getElementById('btn-cancel-delete')?.addEventListener('click', () => closeModal('delete-modal'));
    document.getElementById('btn-confirm-delete')?.addEventListener('click', executeDelete);

    document.getElementById('btn-reminder-dismiss')?.addEventListener('click', dismissActiveReminder);
    document.getElementById('btn-reminder-take')?.addEventListener('click', takeActiveReminder);
}

function openEditModal(id) {
    const med = state.medicines.find(m => m.id === id);
    if (!med) return;

    document.getElementById('edit-id').value = med.id;
    document.getElementById('edit-medicineName').value = med.medicineName;
    document.getElementById('edit-dosage').value = med.dosage;
    document.getElementById('edit-time').value = med.time.substring(0, 5);
    document.getElementById('edit-frequency').value = med.frequency;

    openModal('edit-modal');
}

function openDeleteModal(id, name) {
    state.deletingId = id;
    document.getElementById('delete-medicine-name').textContent = name;
    openModal('delete-modal');
}

async function executeDelete() {
    if (!state.deletingId) return;

    try {
        const response = await fetch(`${API_BASE_URL}/${state.deletingId}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to delete medicine');

        showToast('Medicine deleted successfully.', 'success');
        closeModal('delete-modal');
        state.deletingId = null;
        await loadMedicines();
    } catch (error) {
        console.error('Delete Error:', error);
        showToast('Failed to delete medicine', 'danger');
    }
}

function triggerReminderAlert(medicine) {
    state.activeReminder = medicine;

    document.getElementById('reminder-medicine-name').textContent = medicine.medicineName;
    document.getElementById('reminder-dosage').textContent = medicine.dosage;
    document.getElementById('reminder-time').textContent = formatTime12Hr(medicine.time);

    playReminderSound();
    openModal('reminder-modal');
}

function dismissActiveReminder() {
    if (state.activeReminder) {
        state.dismissedReminderIds.add(state.activeReminder.id);
        state.activeReminder = null;
    }
    closeModal('reminder-modal');
}

async function takeActiveReminder() {
    if (state.activeReminder) {
        await handleMarkAsTaken(state.activeReminder.id);
        state.dismissedReminderIds.add(state.activeReminder.id);
        state.activeReminder = null;
    }
    closeModal('reminder-modal');
}

async function handleMarkAsTaken(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/${id}/take`, {
            method: 'POST'
        });

        if (!response.ok) throw new Error('Failed to mark as taken');

        showToast('✓ Marked as taken for today.', 'success');
        await loadMedicines();
    } catch (error) {
        console.error('Mark Taken Error:', error);
        showToast('Failed to update taken status', 'danger');
    }
}

function checkDueRemindersManual() {
    pollDueMedicines();
    showToast('Checked due reminders.', 'success');
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('active');
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('active');
}

function playReminderSound() {
    try {
        const AudioContext = window.AudioContext || window.webkitAudioContext;
        if (!AudioContext) return;
        const ctx = new AudioContext();

        const osc1 = ctx.createOscillator();
        const gain1 = ctx.createGain();
        osc1.type = 'sine';
        osc1.frequency.setValueAtTime(587.33, ctx.currentTime);
        gain1.gain.setValueAtTime(0.15, ctx.currentTime);
        gain1.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.4);
        osc1.connect(gain1);
        gain1.connect(ctx.destination);
        osc1.start(ctx.currentTime);
        osc1.stop(ctx.currentTime + 0.4);

        const osc2 = ctx.createOscillator();
        const gain2 = ctx.createGain();
        osc2.type = 'sine';
        osc2.frequency.setValueAtTime(880, ctx.currentTime + 0.2);
        gain2.gain.setValueAtTime(0.15, ctx.currentTime + 0.2);
        gain2.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.7);
        osc2.connect(gain2);
        gain2.connect(ctx.destination);
        osc2.start(ctx.currentTime + 0.2);
        osc2.stop(ctx.currentTime + 0.7);
    } catch (e) {
    }
}

function showToast(message, type = 'primary') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    let icon = 'fa-info-circle';
    if (type === 'success') icon = 'fa-circle-check';
    if (type === 'danger') icon = 'fa-triangle-exclamation';

    toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${escapeHtml(message)}</span>`;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

function escapeJsString(str) {
    if (!str) return '';
    return String(str).replace(/'/g, "\\'");
}
