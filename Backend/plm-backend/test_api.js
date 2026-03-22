const http = require('http');

const options = {
  hostname: 'localhost',
  port: 8080,
  path: '/api/auth/login',
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  }
};

const req = http.request(options, (res) => {
  let data = '';
  res.on('data', chunk => data += chunk);
  res.on('end', () => {
    try {
      const parsed = JSON.parse(data);
      const token = parsed.token;
      if (!token) {
        console.error("No token!", data);
        return;
      }
      console.log("Got token:", token.substring(0, 15) + "...");
      
      const searchOptions = {
        hostname: 'localhost',
        port: 8080,
        path: '/api/ecos/search',
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        }
      };
      
      const searchReq = http.request(searchOptions, (searchRes) => {
        console.log('Search Status:', searchRes.statusCode);
        let searchData = '';
        searchRes.on('data', chunk => searchData += chunk);
        searchRes.on('end', () => {
          try {
            console.log('Search Response:', JSON.stringify(JSON.parse(searchData), null, 2));
          } catch(e) {
            console.log('Search Response Text:', searchData);
          }
        });
      });
      
      searchReq.on('error', e => console.error(e));
      searchReq.write(JSON.stringify({
          page: 0,
          size: 10
      }));
      searchReq.end();
    } catch (e) {
      console.error(e, data);
    }
  });
});

req.on('error', (e) => {
  console.error(`Problem with request: ${e.message}`);
});

req.write(JSON.stringify({
  loginId: 'admin',
  password: 'password'
}));
req.end();
