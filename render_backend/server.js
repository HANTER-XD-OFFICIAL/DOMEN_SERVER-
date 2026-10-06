/**
 * Domain Hosting System - Backend API for Render.com
 * Powered by Express & Supabase
 * Project URL: https://yejqvregkkyhxxjblnii.supabase.co
 */

const express = require('express');
const cors = require('cors');
const dns = require('dns').promises;
require('dotenv').config();
const { createClient } = require('@supabase/supabase-js');

const app = express();
const PORT = process.env.PORT || 3000;

// Supabase Configuration
const SUPABASE_URL = process.env.SUPABASE_URL || 'https://yejqvregkkyhxxjblnii.supabase.co';
const SUPABASE_KEY = process.env.SUPABASE_KEY || 'sb_publishable_BSvIvMTDe0rgsZjpnWUxjw_wkWv6I7C';

const supabase = createClient(SUPABASE_URL, SUPABASE_KEY);

// Middleware
app.use(cors({
  origin: '*', // Allow GitHub Pages & custom domains
  methods: ['GET', 'POST', 'PATCH', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'apikey']
}));
app.use(express.json());

// GitHub Pages Official Apex IP addresses
const GITHUB_PAGES_IPS = [
  '185.199.108.153',
  '185.199.109.153',
  '185.199.110.153',
  '185.199.111.153'
];

// Root status
app.get('/', (req, res) => {
  res.json({
    status: 'online',
    service: 'ApexHost Domain Manager Backend',
    platform: 'Render.com',
    supabase_connected: true,
    supabase_url: SUPABASE_URL,
    timestamp: new Date().toISOString()
  });
});

// Health check endpoint (for Render keep-alive and pingers)
app.get('/health', (req, res) => {
  res.status(200).json({ status: 'ok', uptime: process.uptime() });
});

// 1. Get all domain requests
app.get('/api/domains', async (req, res) => {
  try {
    const { status, search } = req.query;
    let query = supabase.from('domain_requests').select('*').order('created_at', { ascending: false });

    if (status && status !== 'all') {
      query = query.eq('status', status);
    }
    if (search) {
      query = query.ilike('domain_name', `%${search}%`);
    }

    const { data, error } = await query;
    if (error) throw error;

    res.json({ success: true, count: data.length, data });
  } catch (err) {
    console.error('Error fetching domains:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// 2. Submit new domain request
app.post('/api/domains', async (req, res) => {
  try {
    const {
      domain_name,
      github_username,
      github_repo,
      client_name,
      client_email,
      request_notes
    } = req.body;

    if (!domain_name || !github_username || !client_email) {
      return res.status(400).json({
        success: false,
        error: 'domain_name, github_username, and client_email are required'
      });
    }

    const cleanDomain = domain_name.trim().toLowerCase();
    const cleanUser = github_username.trim();
    const cleanRepo = (github_repo || `${cleanUser.toLowerCase()}.github.io`).trim();
    const cnameTarget = `${cleanUser.toLowerCase()}.github.io`;
    const token = 'gh-verify-' + Math.random().toString(36).substring(2, 10);

    const newRequest = {
      domain_name: cleanDomain,
      github_username: cleanUser,
      github_repo: cleanRepo,
      client_name: client_name ? client_name.trim() : cleanUser,
      client_email: client_email.trim(),
      request_notes: request_notes || 'Submitted via API',
      status: 'pending',
      ssl_status: 'pending',
      cname_target: cnameTarget,
      dns_verification_token: token,
      dns_configured: false,
      created_at: new Date().toISOString()
    };

    const { data, error } = await supabase
      .from('domain_requests')
      .insert([newRequest])
      .select();

    if (error) throw error;

    res.status(201).json({
      success: true,
      message: `Domain request for ${cleanDomain} created successfully!`,
      data: data[0]
    });
  } catch (err) {
    console.error('Error creating domain request:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// 3. Update domain status (Approve, Activate, Reject, Suspend)
app.patch('/api/domains/:id/status', async (req, res) => {
  try {
    const { id } = req.params;
    const { status, admin_notes, ssl_status } = req.body;

    const allowedStatuses = ['pending', 'approved', 'active', 'rejected', 'suspended'];
    if (status && !allowedStatuses.includes(status)) {
      return res.status(400).json({
        success: false,
        error: `Invalid status. Must be one of: ${allowedStatuses.join(', ')}`
      });
    }

    const updates = {
      updated_at: new Date().toISOString()
    };
    if (status) updates.status = status;
    if (admin_notes !== undefined) updates.admin_notes = admin_notes;
    if (ssl_status) updates.ssl_status = ssl_status;
    if (status === 'approved' || status === 'active') updates.dns_configured = true;

    const { data, error } = await supabase
      .from('domain_requests')
      .update(updates)
      .eq('id', id)
      .select();

    if (error) throw error;

    res.json({
      success: true,
      message: `Domain status updated to ${status}`,
      data: data[0]
    });
  } catch (err) {
    console.error('Error updating domain:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// 4. Delete domain request
app.delete('/api/domains/:id', async (req, res) => {
  try {
    const { id } = req.params;
    const { error } = await supabase.from('domain_requests').delete().eq('id', id);
    if (error) throw error;

    res.json({ success: true, message: `Domain request ${id} deleted` });
  } catch (err) {
    console.error('Error deleting domain:', err);
    res.status(500).json({ success: false, error: err.message });
  }
});

// 5. Server-side Live DNS verification & propagation checker
app.get('/api/dns/check/:domain', async (req, res) => {
  const domain = req.params.domain.toLowerCase().trim();
  const results = {
    domain,
    resolved_ips: [],
    cname_records: [],
    points_to_github: false,
    github_cname_valid: false,
    status: 'unchecked',
    message: ''
  };

  try {
    // Check A records
    try {
      const addresses = await dns.resolve4(domain);
      results.resolved_ips = addresses;
      results.points_to_github = addresses.some(ip => GITHUB_PAGES_IPS.includes(ip));
    } catch (e) {
      // No A records or lookup failed
    }

    // Check CNAME records (if subdomain)
    try {
      const cnames = await dns.resolveCname(domain);
      results.cname_records = cnames;
      results.github_cname_valid = cnames.some(c => c.toLowerCase().endsWith('github.io'));
    } catch (e) {
      // No CNAME records
    }

    if (results.points_to_github || results.github_cname_valid) {
      results.status = 'verified';
      results.message = `DNS correctly propagated to GitHub Pages servers! (${results.resolved_ips.join(', ') || results.cname_records.join(', ')})`;
    } else if (results.resolved_ips.length > 0 || results.cname_records.length > 0) {
      results.status = 'pending_propagation';
      results.message = `Domain resolves to [${results.resolved_ips.join(', ')}], waiting for GitHub Pages routing update.`;
    } else {
      results.status = 'unresolved';
      results.message = 'Domain does not resolve yet. Please add A and CNAME records in your registrar.';
    }

    res.json({ success: true, results });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

// 6. Generate DNS Records guide for any domain
app.get('/api/dns/records/:domain', (req, res) => {
  const domain = req.params.domain.toLowerCase().trim();
  const username = req.query.username || 'username';
  const isApex = domain.split('.').length === 2;
  const cnameTarget = `${username.toLowerCase()}.github.io`;

  const records = [];

  if (isApex) {
    GITHUB_PAGES_IPS.forEach((ip, idx) => {
      records.push({
        type: 'A',
        name: '@',
        value: ip,
        ttl: 3600,
        description: `GitHub Pages Server ${idx + 1}`
      });
    });
    records.push({
      type: 'CNAME',
      name: 'www',
      value: cnameTarget,
      ttl: 3600,
      description: 'Redirect www to GitHub Pages'
    });
  } else {
    const sub = domain.split('.')[0];
    records.push({
      type: 'CNAME',
      name: sub,
      value: cnameTarget,
      ttl: 3600,
      description: 'Subdomain direct mapping to GitHub Pages'
    });
  }

  res.json({
    success: true,
    domain,
    is_apex: isApex,
    cname_file_content: domain,
    records
  });
});

// 7. System Stats
app.get('/api/stats', async (req, res) => {
  try {
    const { data, error } = await supabase.from('domain_requests').select('status, ssl_status');
    if (error) throw error;

    const stats = {
      total: data.length,
      pending: data.filter(d => d.status === 'pending').length,
      approved: data.filter(d => d.status === 'approved').length,
      active: data.filter(d => d.status === 'active').length,
      rejected: data.filter(d => d.status === 'rejected').length,
      ssl_active: data.filter(d => d.ssl_status === 'active').length
    };

    res.json({ success: true, stats });
  } catch (err) {
    res.status(500).json({ success: false, error: err.message });
  }
});

app.listen(PORT, () => {
  console.log(`===============================================`);
  console.log(`ApexHost Render Backend is running on port ${PORT}`);
  console.log(`Connected to Supabase: ${SUPABASE_URL}`);
  console.log(`===============================================`);
});
