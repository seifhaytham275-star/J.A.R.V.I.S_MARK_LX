# عم أورا (AURA AI)

A lightweight browser-based AI chat prototype with a modern, Arabic-first blue/purple sci-fi interface and selectable Gemini model.

## Run
Open `index.html` in a modern browser. Paste your own Gemini API key and press **Use key for this session**.

## API key safety
- The app does not persist the key to local storage.
- The browser sends the key directly to Google's Gemini API.
- Do not publish a key, commit it to GitHub, or share screenshots containing it.
- This client-side approach is for personal prototyping only. For a public deployment, use a backend proxy and keep the API key in server-side secret storage.

## Models
The default is `gemini-3.8-flash`; `gemini-2.5-flash` is included as an alternate. Availability depends on Google's current model access, account eligibility, quota, and API support.
