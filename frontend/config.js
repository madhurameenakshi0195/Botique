
window.SITE = {
  // Use http://localhost:4000
  api:
      window.location.hostname === "localhost" ||
      window.location.hostname === "127.0.0.1"
          ? "http://localhost:4000"
          : "https://YOUR-RENDER-BACKEND-URL",
  owner: {
    name: "Rani",
    phone: "7659068530",
    address: "Madhura Sarees Opp to smart wonders Vidhyanagar Colony rd no.7-8,jadcherla ,Mahbubnagar "   // add the address here
  }
};
