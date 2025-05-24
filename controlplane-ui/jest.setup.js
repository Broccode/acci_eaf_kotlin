const { TextEncoder, TextDecoder } = require('node:util');

// Polyfill for TextEncoder and TextDecoder
global.TextEncoder = TextEncoder;
global.TextDecoder = TextDecoder;

// Mock fetch globally
global.fetch = jest.fn();

// Mock window.location with all required properties
const mockLocation = {
  href: 'http://localhost:3000/',
  origin: 'http://localhost:3000',
  protocol: 'http:',
  host: 'localhost:3000',
  hostname: 'localhost',
  port: '3000',
  pathname: '/',
  search: '',
  hash: '',
  assign: jest.fn(),
  replace: jest.fn(),
  reload: jest.fn(),
  toString: () => 'http://localhost:3000/',
};

Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true,
});

// Mock URL.searchParams
global.URLSearchParams = class URLSearchParams {
  constructor(init) {
    this.params = new Map();
    if (typeof init === 'string') {
      let searchString = init;
      if (searchString.startsWith('?')) {
        searchString = searchString.slice(1);
      }
      for (const pair of searchString.split('&')) {
        if (pair) {
          const [key, value] = pair.split('=');
          this.params.set(decodeURIComponent(key), decodeURIComponent(value || ''));
        }
      }
    }
  }

  append(name, value) { this.params.set(name, value); }
  delete(name) { this.params.delete(name); }
  get(name) { return this.params.get(name); }
  has(name) { return this.params.has(name); }
  set(name, value) { this.params.set(name, value); }
  toString() {
    const params = [];
    for (const [key, value] of this.params) {
      params.push(`${encodeURIComponent(key)}=${encodeURIComponent(value)}`);
    }
    return params.join('&');
  }
};
