package com.example.data.supabase

object SupabaseConfig {
    const val PROJECT_URL = "https://yejqvregkkyhxxjblnii.supabase.co"
    const val ANON_KEY = "sb_publishable_BSvIvMTDe0rgsZjpnWUxjw_wkWv6I7C"
    const val REST_BASE_URL = "$PROJECT_URL/rest/v1/"
    const val TABLE_NAME = "domain_requests"

    // SQL Schema for user's Supabase dashboard
    val SQL_SCHEMA = """
        -- Domain Hosting System Schema for Supabase
        -- Run this in your Supabase SQL Editor:
        
        CREATE TABLE IF NOT EXISTS domain_requests (
            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
            domain_name TEXT NOT NULL,
            github_username TEXT NOT NULL,
            github_repo TEXT NOT NULL,
            client_name TEXT NOT NULL,
            client_email TEXT NOT NULL,
            request_notes TEXT,
            status TEXT NOT NULL DEFAULT 'pending', -- pending, approved, active, rejected, suspended
            ssl_status TEXT NOT NULL DEFAULT 'pending', -- pending, active, failed
            cname_target TEXT,
            dns_verification_token TEXT,
            dns_configured BOOLEAN DEFAULT false,
            admin_notes TEXT,
            created_at TIMESTAMPTZ DEFAULT now(),
            updated_at TIMESTAMPTZ DEFAULT now()
        );

        -- Enable Row Level Security (RLS)
        ALTER TABLE domain_requests ENABLE ROW LEVEL SECURITY;

        -- Allow public read and submission for GitHub Pages client portal
        CREATE POLICY "Allow public read of domain requests" 
        ON domain_requests FOR SELECT USING (true);

        CREATE POLICY "Allow public insert of domain requests" 
        ON domain_requests FOR INSERT WITH CHECK (true);

        -- Allow updates for admin panel
        CREATE POLICY "Allow public updates of domain requests" 
        ON domain_requests FOR UPDATE USING (true);

        -- Allow deletion
        CREATE POLICY "Allow public delete of domain requests" 
        ON domain_requests FOR DELETE USING (true);
    """.trimIndent()
}
