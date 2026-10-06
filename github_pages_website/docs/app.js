// ========================================================
// ApexHost - GitHub Pages Client Script with Supabase
// Project URL: https://yejqvregkkyhxxjblnii.supabase.co
// Anon Key: sb_publishable_BSvIvMTDe0rgsZjpnWUxjw_wkWv6I7C
// ========================================================

const SUPABASE_URL = "https://yejqvregkkyhxxjblnii.supabase.co";
const SUPABASE_ANON_KEY = "sb_publishable_BSvIvMTDe0rgsZjpnWUxjw_wkWv6I7C";

// Initialize the Supabase Client
const supabase = window.supabase ? window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY) : null;

// Elements
const domainForm = document.getElementById('domainRequestForm');
const formAlert = document.getElementById('formAlert');
const submitBtn = document.getElementById('submitBtn');

const checkDomainInput = document.getElementById('checkDomainInput');
const lookupBtn = document.getElementById('lookupBtn');
const statusResult = document.getElementById('statusResult');

const domainInput = document.getElementById('domainInput');
const cnamePreviewCode = document.getElementById('cnamePreviewCode');
const copyCnameBtn = document.getElementById('copyCnameBtn');

// Dynamic CNAME preview as user types
if (domainInput && cnamePreviewCode) {
  domainInput.addEventListener('input', (e) => {
    const val = e.target.value.trim().toLowerCase();
    cnamePreviewCode.innerText = val || "yourdomain.com";
  });
}

// Copy CNAME button
if (copyCnameBtn && cnamePreviewCode) {
  copyCnameBtn.addEventListener('click', () => {
    navigator.clipboard.writeText(cnamePreviewCode.innerText);
    const orig = copyCnameBtn.innerText;
    copyCnameBtn.innerText = "Copied!";
    setTimeout(() => { copyCnameBtn.innerText = orig; }, 1800);
  });
}

// 1. Submit Custom Domain Request to Supabase
if (domainForm) {
  domainForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    formAlert.className = "alert hidden";
    submitBtn.disabled = true;
    submitBtn.innerHTML = `<span>Saving to Supabase...</span>`;

    const domainName = document.getElementById('domainInput').value.trim().toLowerCase();
    const githubUser = document.getElementById('githubUserInput').value.trim();
    const githubRepo = document.getElementById('githubRepoInput').value.trim();
    const clientName = document.getElementById('clientNameInput').value.trim();
    const clientEmail = document.getElementById('clientEmailInput').value.trim();
    const notes = document.getElementById('notesInput').value.trim();

    const cnameTarget = `${githubUser.toLowerCase()}.github.io`;
    const token = 'gh-verify-' + Math.random().toString(36).substring(2, 10);

    const payload = {
      domain_name: domainName,
      github_username: githubUser,
      github_repo: githubRepo,
      client_name: clientName,
      client_email: clientEmail,
      request_notes: notes || "Submitted via GitHub Pages portal",
      status: "pending",
      ssl_status: "pending",
      cname_target: cnameTarget,
      dns_verification_token: token,
      dns_configured: false
    };

    try {
      if (!supabase) {
        throw new Error("Supabase SDK not loaded");
      }

      const { data, error } = await supabase
        .from('domain_requests')
        .insert([payload])
        .select();

      if (error) {
        throw error;
      }

      formAlert.className = "alert success";
      formAlert.innerHTML = `
        <strong>🎉 Request Dispatched Successfully!</strong><br>
        Your request for <code>${domainName}</code> has been received. Your admin will review and provision DNS records shortly.<br>
        Target CNAME: <code>${cnameTarget}</code>
      `;
      domainForm.reset();
      cnamePreviewCode.innerText = domainName;
    } catch (err) {
      console.error("Supabase submission error:", err);
      formAlert.className = "alert error";
      formAlert.innerHTML = `
        <strong>Submission Notice:</strong> ${err.message || "Failed to communicate with Supabase backend."}<br>
        <em>Ensure the database schema has been initialized in your Supabase project dashboard.</em>
      `;
    } finally {
      submitBtn.disabled = false;
      submitBtn.innerHTML = `<span>Submit Request to Supabase</span><span class="btn-arrow">→</span>`;
    }
  });
}

// 2. Lookup Existing Domain Status
if (lookupBtn && checkDomainInput) {
  lookupBtn.addEventListener('click', async () => {
    const query = checkDomainInput.value.trim().toLowerCase();
    if (!query) return;

    lookupBtn.disabled = true;
    lookupBtn.innerText = "Searching...";
    statusResult.className = "status-result";
    statusResult.innerHTML = "Querying Supabase database...";

    try {
      if (!supabase) throw new Error("Supabase SDK unavailable");

      const { data, error } = await supabase
        .from('domain_requests')
        .select('*')
        .ilike('domain_name', `%${query}%`)
        .limit(1);

      if (error) throw error;

      if (!data || data.length === 0) {
        statusResult.innerHTML = `
          <div style="color: var(--warning)">No records found for "<strong>${query}</strong>". Submit a request above!</div>
        `;
      } else {
        const item = data[0];
        const statusClass = `status-${item.status.toLowerCase()}`;
        statusResult.innerHTML = `
          <div><strong>Domain:</strong> <code>${item.domain_name}</code></div>
          <div style="margin-top: 4px;"><strong>Status:</strong> <span class="status-pill ${statusClass}">${item.status.toUpperCase()}</span></div>
          <div style="margin-top: 4px;"><strong>Target:</strong> <code>${item.github_username}/${item.github_repo}</code></div>
          <div style="margin-top: 4px;"><strong>CNAME:</strong> <code>${item.cname_target || item.github_username + '.github.io'}</code></div>
          <div style="margin-top: 4px;"><strong>SSL Status:</strong> ${item.ssl_status || 'Pending'}</div>
          ${item.admin_notes ? `<div style="margin-top: 6px; color: var(--text-muted); font-size: 0.8rem">Admin Note: ${item.admin_notes}</div>` : ''}
        `;
      }
    } catch (err) {
      statusResult.innerHTML = `<span style="color: var(--danger)">Lookup error: ${err.message}</span>`;
    } finally {
      lookupBtn.disabled = false;
      lookupBtn.innerText = "Lookup";
    }
  });
}
