const { setWorldConstructor } = require('@cucumber/cucumber');
const { chromium } = require('playwright');

class CustomWorld {
  constructor(options) {
    this.attach = options.attach;
    this.parameters = options.parameters;
    this.browser = null;
    this.context = null;
    this.page = null;
    this.apiContext = null;
  }

  async init() {
    this.browser = await chromium.launch({ 
      headless: process.env.HEADLESS !== 'false',
      slowMo: process.env.SLOW_MO ? parseInt(process.env.SLOW_MO) : 0 
    });
    
    this.context = await this.browser.newContext({
      viewport: { width: 1280, height: 720 },
      ignoreHTTPSErrors: true
    });
    
    this.page = await this.context.newPage();
    
    // Setup API context for backend calls
    this.apiContext = await this.context.request;
    
    // Setup base URL
    this.baseURL = process.env.BASE_URL || 'http://localhost:3000';
    
    // Enable console logging in tests
    this.page.on('console', msg => console.log(`PAGE LOG: ${msg.text()}`));
    this.page.on('pageerror', error => console.log(`PAGE ERROR: ${error.message}`));
  }

  async cleanup() {
    if (this.page) await this.page.close();
    if (this.context) await this.context.close();
    if (this.browser) await this.browser.close();
  }
}

setWorldConstructor(CustomWorld);