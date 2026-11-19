# Madhura Sarees

Website for a saree, embroidery, jewellery and cloth business.

- `frontend/` static website (Vercel): landing page, products with category chooser, admin page
- `backend/` Java 21 + Spring Boot REST API (Render) with Neon Postgres

Visitors only browse; no login, no wishlist. Only the admin can log in and add, edit or delete products.

## 1. Database (Neon)
1. Create a project at neon.tech. In Connection details note the host, database name, user and password.
2. Build the JDBC address like this (keep `sslmode=require`):
   `jdbc:postgresql://<host>/<database>?sslmode=require`
3. The tables (`products`, `admins`) are created automatically the first time the backend starts.

## 2. Run locally
You need JDK 21 and Maven.
```bash
cd backend
# set the variables listed in .env.example, for example on Mac/Linux:
export DB_URL="jdbc:postgresql://ep-xxxx.region.aws.neon.tech/neondb?sslmode=require"
export DB_USER=neondb_owner DB_PASSWORD=... JWT_SECRET=<32+ random characters>
export ADMIN_EMAIL=rani@example.com ADMIN_PASSWORD=...
export FRONTEND_URL=http://localhost:5500
mvn spring-boot:run          # http://localhost:4000
```
(In IntelliJ, add the same names under Run Configuration > Environment variables.)
In a second terminal serve the frontend:
```bash
cd frontend
npx serve -l 5500
```
Open http://localhost:5500 and http://localhost:5500/admin.html.

## 3. Push to Git
```bash
git init && git add . && git commit -m "Madhura Sarees"
git branch -M main
git remote add origin https://github.com/<you>/madhura-sarees.git
git push -u origin main
```

## 4. Deploy the backend (Render)
New > Web Service > pick the repo.
- Language: Docker
- Root Directory: `backend` (Render uses `backend/Dockerfile`)
- Environment variables: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `FRONTEND_URL` (your Vercel address, no trailing slash)

Copy the Render URL, e.g. `https://madhura-api.onrender.com`. Opening it should show `{"ok":true,...}`.

## 5. Deploy the frontend (Vercel)
1. Edit `frontend/config.js`: set `api` to the Render URL and fill in Rani's `phone` and `address`. Commit and push.
2. Vercel > Add New Project > pick the repo, Root Directory `frontend`, Framework Preset "Other", Deploy.
3. Put the Vercel address into `FRONTEND_URL` on Render (several addresses can be separated by commas) and redeploy.

## Admin
Open `/admin.html` on your site and log in with `ADMIN_EMAIL` and `ADMIN_PASSWORD`. To change the password, change `ADMIN_PASSWORD` on Render and redeploy.
Product photos use an image link (for example from Cloudinary or Imgur). Without a link the product shows a coloured pattern.

## API
| Method | Path | Who |
|---|---|---|
| GET | `/api/products?category=&sub=` | everyone |
| GET | `/api/products/{id}` | everyone |
| POST | `/api/admin/login` | admin (returns a 12 hour token) |
| POST, PUT, DELETE | `/api/products`, `/api/products/{id}` | admin token only |

## Notes
- Render's free plan sleeps after inactivity, and a Java app takes a little longer to wake up (about a minute).
- Login attempts are limited to 10 per 15 minutes per address.
