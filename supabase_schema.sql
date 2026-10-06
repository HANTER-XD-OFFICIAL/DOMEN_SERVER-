-- ========================================================
-- Domain Hosting System - Supabase PostgreSQL Schema
-- Project URL: https://yejqvregkkyhxxjblnii.supabase.co
-- ========================================================

-- 1. Create the domain_requests table
CREATE TABLE IF NOT EXISTS domain_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    domain_name TEXT NOT NULL UNIQUE,
    github_username TEXT NOT NULL,
    github_repo TEXT NOT NULL,
    client_name TEXT NOT NULL,
    client_email TEXT NOT NULL,
    request_notes TEXT,
    status TEXT NOT NULL DEFAULT 'pending', -- 'pending', 'approved', 'active', 'rejected', 'suspended'
    ssl_status TEXT NOT NULL DEFAULT 'pending', -- 'pending', 'active', 'failed'
    cname_target TEXT,
    dns_verification_token TEXT,
    dns_configured BOOLEAN DEFAULT false,
    admin_notes TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

-- 2. Create index on status and domain_name for fast lookups
CREATE INDEX IF NOT EXISTS idx_domain_requests_status ON domain_requests(status);
CREATE INDEX IF NOT EXISTS idx_domain_requests_domain ON domain_requests(domain_name);

-- 3. Enable Row Level Security (RLS)
ALTER TABLE domain_requests ENABLE ROW LEVEL SECURITY;

-- 4. RLS Policies
-- Allow public select for the GitHub Pages client portal (checking domain status)
CREATE POLICY "Allow public read access"
ON domain_requests FOR SELECT
TO anon, authenticated
USING (true);

-- Allow public submission of domain requests from the GitHub Pages website
CREATE POLICY "Allow public insert requests"
ON domain_requests FOR INSERT
TO anon, authenticated
WITH CHECK (true);

-- Allow updates (status changes, DNS configuration, admin notes)
CREATE POLICY "Allow updates for domain requests"
ON domain_requests FOR UPDATE
TO anon, authenticated
USING (true);

-- Allow deletion
CREATE POLICY "Allow deletion of domain requests"
ON domain_requests FOR DELETE
TO anon, authenticated
USING (true);

-- 5. Seed initial demo record
INSERT INTO domain_requests (
    domain_name,
    github_username,
    github_repo,
    client_name,
    client_email,
    request_notes,
    status,
    ssl_status,
    cname_target,
    dns_verification_token,
    dns_configured,
    admin_notes
) VALUES (
    'alexdev.me',
    'alexraselchodhury',
    'alexraselchodhury.github.io',
    'Alex Rasel',
    'alexraselchodhury@gmail.com',
    'Portfolio and developer documentation site hosted on GitHub Pages',
    'approved',
    'active',
    'alexraselchodhury.github.io',
    'gh-verify-8924b17a',
    true,
    'Approved. Ready for DNS propagation.'
) ON CONFLICT (domain_name) DO NOTHING;
