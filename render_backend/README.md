# 🚀 Domain Hosting Backend (Render.com + Supabase)

এই ব্যাক-এন্ডটি **Render.com**-এ হোস্ট করার জন্য সম্পূর্ণ তৈরি করা হয়েছে। এটি এক্সপ্রেস (Node.js) এবং আপনার **Supabase** ডেটাবেস (`https://yejqvregkkyhxxjblnii.supabase.co`) এর সাথে সরাসরি সংযুক্ত।

---

## 🛠️ Render.com এ ডিপ্লয় করার সহজ ৩টি ধাপ:

### ধাপ ১: GitHub-এ রিপোজিটরি তৈরি করুন
1. এই ফোল্ডারের ফাইলগুলো (`package.json`, `server.js`, `render.yaml`) আপনার GitHub রিপোজিটরিতে পুশ করুন (অথবা একটি নতুন রিপোজিটরি বানান যেমন: `domain-backend`)।

### ধাপ ২: Render.com এ লগইন করুন
1. [https://dashboard.render.com](https://dashboard.render.com)-এ যান।
2. **New +** বাটনে ক্লিক করে **Web Service** সিলেক্ট করুন।
3. আপনার GitHub অ্যাকাউন্ট কানেক্ট করে আপনার রিপোজিটরি নির্বাচন করুন।

### ধাপ ৩: সেটিংস কনফিগার করুন
- **Name:** `domain-hosting-backend`
- **Runtime:** `Node`
- **Build Command:** `npm install`
- **Start Command:** `node server.js`
- **Instance Type:** `Free`

**Environment Variables যোগ করুন:**
- `SUPABASE_URL` = `https://yejqvregkkyhxxjblnii.supabase.co`
- `SUPABASE_KEY` = `sb_publishable_BSvIvMTDe0rgsZjpnWUxjw_wkWv6I7C`
- `NODE_ENV` = `production`

এরপর **Create Web Service** বাটনে ক্লিক করুন! রেন্ডার ২ মিনিটের মধ্যে আপনার ব্যাকএন্ড লাইভ করে দিবে এবং একটি URL দিবে (যেমন: `https://domain-hosting-backend.onrender.com`)।

---

## 📡 API Endpoints

| মেথড | এন্ডপয়েন্ট | বিবরণ |
|---|---|---|
| `GET` | `/health` | Render হেলথ চেক এবং সার্ভার পিং |
| `GET` | `/api/domains` | সব ডোমেইন রিকোয়েস্ট দেখা |
| `POST` | `/api/domains` | নতুন ডোমেইন রিকোয়েস্ট সাবমিট করা |
| `PATCH` | `/api/domains/:id/status` | ডোমেইন অ্যাপ্রুভ / অ্যাক্টিভ / রিজেক্ট করা |
| `GET` | `/api/dns/check/:domain` | লাইভ DNS চেক করা (GitHub Pages এ পয়েন্ট করছে কিনা) |
| `GET` | `/api/dns/records/:domain` | নির্দিষ্ট ডোমেইনের জন্য DNS জোন রেকর্ড তৈরি করা |
| `GET` | `/api/stats` | ড্যাশবোর্ড মেট্রিক্স (পেন্ডিং, অ্যাক্টিভ সংখ্যা) |

---

## ⚠️ GitHub Pages 404 সমস্যার সমাধান (Screenshot Issue Solution):

আপনার স্ক্রিনশট অনুযায়ী:
GitHub Settings > Pages-এ ব্রাঞ্চ অপশনে **`/docs`** সিলেক্ট করা আছে:
`main` ▾ `/docs` ▾

**সমাধান:**
1. GitHub-এ আপনার রিপোজিটরি `DOMEN_SERVER-` ওপেন করে **Settings** > **Pages**-এ যান।
2. **Branch** সেকশনে যেখানে `/docs` সিলেক্ট করা আছে, সেই ড্রপডাউনে ক্লিক করে **`/ (root)`** সিলেক্ট করুন এবং **Save** করুন।
3. অথবা আপনার ফাইলের ভেতর একটি `docs` নামের ফোল্ডার বানিয়ে তার ভেতরে `index.html`, `styles.css`, `app.js` রাখুন।
4. Custom Domain: `domenserver.nl8.eu` এর জন্য DNS প্রোভাইডারে CNAME রেকর্ড যোগ করুন:
   - Name: `domenserver`
   - Target: `hanter-xd-official.github.io`
