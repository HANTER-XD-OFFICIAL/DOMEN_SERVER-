# ApexHost - Custom Domain Client Portal for GitHub Pages

This is the static frontend portal for the **Domain Hosting System**, designed to be hosted directly on **GitHub Pages** and connected to the **Supabase Backend Database**.

## Configured Supabase Credentials
- **Project URL:** `https://yejqvregkkyhxxjblnii.supabase.co`
- **Anon Key:** `sb_publishable_BSvIvMTDe0rgsZjpnWUxjw_wkWv6I7C`
- **Table Name:** `domain_requests`

---

## 🚀 How to Host on GitHub Pages (in 2 Minutes)

### Option 1: Direct GitHub Web UI
1. Create a new GitHub repository (e.g. `domain-portal` or `<your-username>.github.io`).
2. Upload the files in this directory (`index.html`, `styles.css`, `app.js`, `CNAME`).
3. Navigate to **Settings** > **Pages**.
4. Under **Build and deployment**:
   - Source: **Deploy from a branch**
   - Branch: **main** (or **gh-pages**) / Folder: **/ (root)**
   - Click **Save**.
5. Your custom website is live within seconds!

### Option 2: Git Command Line
```bash
git init
git add .
git commit -m "Deploy ApexHost portal to GitHub Pages"
git branch -M main
git remote add origin https://github.com/<your-username>/<your-repo>.git
git push -u origin main
```

---

## ⚙️ Setting Up Your Custom Domain on GitHub Pages
1. In your repository on GitHub, open **Settings** > **Pages**.
2. In **Custom domain**, enter your domain name (e.g., `portal.example.com` or `example.com`).
3. Add the DNS records shown in the portal table at your DNS provider:
   - For Apex Domains (`example.com`):
     - `A @ 185.199.108.153`
     - `A @ 185.199.109.153`
     - `A @ 185.199.110.153`
     - `A @ 185.199.111.153`
     - `CNAME www <your-username>.github.io`
   - For Subdomains (`portal.example.com`):
     - `CNAME portal <your-username>.github.io`
4. Once DNS propagates, check **Enforce HTTPS** in GitHub Pages settings.
