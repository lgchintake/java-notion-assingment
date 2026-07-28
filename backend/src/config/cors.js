const cors = require("cors");
const { env } = require("./env");

const corsPolicy = cors({
  origin: env.UI_URL,
  methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
  allowedHeaders: ["Content-Type",
  "Accept",
  "Origin",
  "Authorization",
  "X-CSRF-TOKEN",],
  credentials: true,
});

module.exports = { corsPolicy };
