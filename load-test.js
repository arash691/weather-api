import http from 'k6/http';
import {check, sleep} from 'k6';

export let options = {
    stages: [
        {duration: '10s', target: 5},  // Ramp up to 5 virtual users over 10 seconds
        {duration: '30s', target: 5},  // Stay at 5 virtual users for 30 seconds
        {duration: '5s', target: 0},   // Ramp down to 0 virtual users over 5 seconds
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'], // http errors should be less than 1%
        http_req_duration: ['p(95)<200'], // 95% of requests should be below 200ms
    },
};

export default function () {
    // Test the weather summary endpoint
    let res = http.get('http://localhost:8080/api/v1/weather/summary?locations=51.5074,-0.1278&temperature=20&unit=celsius');

    check(res, {
        'is status 200 or 429': (r) => r.status === 200 || r.status === 429,
        'rate limit header present': (r) => r.headers['X-RateLimit-Remaining'] !== undefined,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    // Test the location endpoint occasionally
    if (Math.random() < 0.3) {
        let locationRes = http.get('http://localhost:8080/api/v1/weather/locations/51.5074,-0.1278');
        check(locationRes, {
            'location endpoint responds': (r) => r.status < 500,
        });
    }


    sleep(1); // Simulate user think time
}