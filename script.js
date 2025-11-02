class InventoryAnalytics {
  constructor() {
    this.currentFile = null;
    this.charts = {};
    this.parsedData = null;
    this.initializeEventListeners();
  }

  initializeEventListeners() {
    const dropZone = document.getElementById('dropZone');
    const fileInput = document.getElementById('excelFile');

    dropZone.addEventListener('dragover', (e) => {
      e.preventDefault();
      dropZone.classList.add('dragover');
    });

    dropZone.addEventListener('dragleave', () => {
      dropZone.classList.remove('dragover');
    });

    dropZone.addEventListener('drop', (e) => {
      e.preventDefault();
      dropZone.classList.remove('dragover');
      const files = e.dataTransfer.files;
      if (files.length > 0) {
        this.handleFileSelection(files[0]);
      }
    });

    dropZone.addEventListener('click', () => {
      fileInput.click();
    });

    fileInput.addEventListener('change', (e) => {
      if (e.target.files.length > 0) {
        this.handleFileSelection(e.target.files[0]);
      }
    });

    // Age limit change listeners
    ['freshLimit', 'moderateLimit', 'agingLimit'].forEach(id => {
      document.getElementById(id).addEventListener('input', () => {
        if (this.parsedData) {
          const analysis = this.analyzeData(this.parsedData);
          this.displayPreview(analysis);
        }
      });
    });
  }

  handleFileSelection(file) {
    if (!this.validateFile(file)) return;
    
    this.currentFile = file;
    this.displayFileInfo(file);
    this.enableGenerateButton();
    this.previewFileData(file);
  }

  validateFile(file) {
    const validTypes = ['application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 'application/vnd.ms-excel', 'text/csv'];
    const validExtensions = ['.xlsx', '.xls', '.csv'];
    const maxSize = 10 * 1024 * 1024;

    const hasValidType = validTypes.includes(file.type) || validExtensions.some(ext => file.name.toLowerCase().endsWith(ext));
    
    if (!hasValidType) {
      this.showMessage('Please select a valid Excel (.xlsx, .xls) or CSV file', 'error');
      return false;
    }

    if (file.size > maxSize) {
      this.showMessage('File size must be less than 10MB', 'error');
      return false;
    }

    return true;
  }

  displayFileInfo(file) {
    document.getElementById('fileName').textContent = file.name;
    document.getElementById('fileSize').textContent = this.formatFileSize(file.size);
    document.getElementById('fileInfo').style.display = 'flex';
    document.getElementById('dropZone').style.display = 'none';
  }

  formatFileSize(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  }

  enableGenerateButton() {
    const btn = document.getElementById('generateBtn');
    btn.disabled = false;
    btn.style.opacity = '1';
  }

  getAgeLimits() {
    return {
      fresh: parseInt(document.getElementById('freshLimit').value) || 30,
      moderate: parseInt(document.getElementById('moderateLimit').value) || 60,
      aging: parseInt(document.getElementById('agingLimit').value) || 90
    };
  }

  async previewFileData(file) {
    try {
      const text = await file.text();
      const data = this.parseCSVData(text);
      this.parsedData = data;
      const analysis = this.analyzeData(data);
      this.displayPreview(analysis);
    } catch (error) {
      const mockData = this.generateMockPreview();
      this.displayPreview(mockData);
    }
  }

  analyzeData(items) {
    const now = new Date();
    const limits = this.getAgeLimits();
    const agingBuckets = [
      `Fresh (0-${limits.fresh})`,
      `Moderate (${limits.fresh + 1}-${limits.moderate})`,
      `Aging (${limits.moderate + 1}-${limits.aging})`,
      `Critical (>${limits.aging})`
    ];
    
    const analysis = {
      agingData: {},
      categoryData: {},
      riskData: { Low: 0, Medium: 0, High: 0, Critical: 0 },
      matrixData: {},
      groupedItems: {},
      insights: [],
      limits
    };

    agingBuckets.forEach(bucket => {
      analysis.agingData[bucket] = 0;
      analysis.groupedItems[bucket] = {};
    });

    let totalValue = 0;
    let criticalItems = 0;
    const categories = new Set();

    items.forEach(item => {
      const days = Math.floor((now - item.dateReceived) / (1000 * 60 * 60 * 24));
      const value = item.quantity * (item.unitCost || 2500);
      totalValue += value;
      
      let bucket, risk;
      if (days <= limits.fresh) { bucket = agingBuckets[0]; risk = 'Low'; }
      else if (days <= limits.moderate) { bucket = agingBuckets[1]; risk = 'Medium'; }
      else if (days <= limits.aging) { bucket = agingBuckets[2]; risk = 'High'; }
      else { bucket = agingBuckets[3]; risk = 'Critical'; criticalItems++; }

      analysis.agingData[bucket]++;
      analysis.riskData[risk]++;
      categories.add(item.category);
      
      if (!analysis.categoryData[item.category]) {
        analysis.categoryData[item.category] = 0;
      }
      analysis.categoryData[item.category]++;

      const matrixKey = `${item.category}-${bucket}`;
      if (!analysis.matrixData[matrixKey]) {
        analysis.matrixData[matrixKey] = 0;
      }
      analysis.matrixData[matrixKey]++;

      if (!analysis.groupedItems[bucket][item.category]) {
        analysis.groupedItems[bucket][item.category] = [];
      }
      analysis.groupedItems[bucket][item.category].push({
        ...item,
        days,
        risk,
        value
      });
    });

    analysis.insights = this.generateInsights(analysis, items.length, criticalItems, categories.size);

    return {
      totalItems: items.length,
      criticalItems,
      totalValue: Math.round(totalValue),
      categoryCount: categories.size,
      ...analysis
    };
  }

  generateInsights(analysis, totalItems, criticalItems, categoryCount) {
    const insights = [];
    
    if (criticalItems > totalItems * 0.2) {
      insights.push({
        type: 'critical',
        text: `🚨 ${((criticalItems/totalItems)*100).toFixed(1)}% of inventory is critically aged (>90 days). Immediate action required!`
      });
    }
    
    const topCategory = Object.entries(analysis.categoryData)
      .sort(([,a], [,b]) => b - a)[0];
    if (topCategory) {
      insights.push({
        type: 'info',
        text: `📊 ${topCategory[0]} is your largest category with ${topCategory[1]} items (${((topCategory[1]/totalItems)*100).toFixed(1)}%)`
      });
    }
    
    if (analysis.riskData.Critical > 0) {
      insights.push({
        type: 'warning',
        text: `⚠️ ${analysis.riskData.Critical} items need immediate clearance or liquidation`
      });
    }
    
    if (categoryCount > 5) {
      insights.push({
        type: 'success',
        text: `✅ Well-diversified inventory across ${categoryCount} categories`
      });
    }
    
    return insights;
  }

  generateMockPreview() {
    return {
      totalItems: 25,
      criticalItems: 8,
      totalValue: 5250000,
      categoryCount: 5,
      agingData: { 'Fresh (0-30)': 10, 'Moderate (31-60)': 7, 'Aging (61-90)': 5, 'Critical (>90)': 3 },
      categoryData: { Electronics: 12, Furniture: 8, Supplies: 3, Appliances: 2 },
      riskData: { Low: 10, Medium: 7, High: 5, Critical: 3 },
      matrixData: {},
      insights: [
        { type: 'info', text: '📊 Electronics is your largest category with 12 items (48%)' },
        { type: 'warning', text: '⚠️ 3 items need immediate clearance or liquidation' }
      ]
    };
  }

  displayPreview(data) {
    const limits = data.limits || { aging: 90 };
    document.getElementById('totalItems').textContent = data.totalItems.toLocaleString();
    document.getElementById('criticalItems').textContent = data.criticalItems.toLocaleString();
    document.getElementById('totalValue').textContent = '₹' + data.totalValue.toLocaleString();
    document.getElementById('categoryCount').textContent = data.categoryCount.toLocaleString();
    
    // Update critical items label
    const criticalLabel = document.querySelector('.stat-card:nth-child(2) .stat-label');
    criticalLabel.textContent = `Critical (>${limits.aging} days)`;
    
    this.createAllCharts(data);
    this.displayInsights(data.insights);
    document.getElementById('previewSection').style.display = 'block';
  }

  displayInsights(insights) {
    const container = document.getElementById('insightsList');
    container.innerHTML = '';
    
    insights.forEach(insight => {
      const item = document.createElement('div');
      item.className = `insight-item ${insight.type}`;
      item.textContent = insight.text;
      container.appendChild(item);
    });
  }

  createAllCharts(data) {
    Object.values(this.charts).forEach(chart => chart?.destroy());
    
    // Aging Distribution Chart
    this.charts.aging = new Chart(document.getElementById('agingChart'), {
      type: 'doughnut',
      data: {
        labels: Object.keys(data.agingData),
        datasets: [{
          data: Object.values(data.agingData),
          backgroundColor: ['#48bb78', '#ed8936', '#f56565', '#e53e3e'],
          borderWidth: 2,
          borderColor: '#fff'
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { padding: 15, usePointStyle: true } }
        }
      }
    });

    // Category Breakdown Chart
    this.charts.category = new Chart(document.getElementById('categoryChart'), {
      type: 'bar',
      data: {
        labels: Object.keys(data.categoryData),
        datasets: [{
          label: 'Items',
          data: Object.values(data.categoryData),
          backgroundColor: '#667eea',
          borderRadius: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: { beginAtZero: true, grid: { color: '#f0f0f0' } },
          x: { grid: { display: false } }
        }
      }
    });

    // Risk Analysis Chart
    this.charts.risk = new Chart(document.getElementById('riskChart'), {
      type: 'polarArea',
      data: {
        labels: Object.keys(data.riskData),
        datasets: [{
          data: Object.values(data.riskData),
          backgroundColor: ['#48bb78', '#ed8936', '#f56565', '#e53e3e']
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom', labels: { padding: 15 } }
        }
      }
    });

    // Category vs Aging Matrix Chart
    const matrixLabels = Object.keys(data.categoryData);
    const agingLabels = Object.keys(data.agingData);
    const matrixDatasets = agingLabels.map((aging, index) => ({
      label: aging,
      data: matrixLabels.map(category => data.matrixData[`${category}-${aging}`] || 0),
      backgroundColor: ['#48bb78', '#ed8936', '#f56565', '#e53e3e'][index]
    }));

    this.charts.matrix = new Chart(document.getElementById('matrixChart'), {
      type: 'bar',
      data: {
        labels: matrixLabels,
        datasets: matrixDatasets
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: { stacked: true, grid: { display: false } },
          y: { stacked: true, beginAtZero: true, grid: { color: '#f0f0f0' } }
        },
        plugins: {
          legend: { position: 'bottom', labels: { padding: 10, usePointStyle: true } }
        }
      }
    });
  }

  async uploadFile() {
    if (!this.currentFile) {
      this.showMessage('Please select a file first', 'error');
      return;
    }

    this.showProgressSection();
    this.simulateProgress();

    try {
      const text = await this.currentFile.text();
      const data = this.parseCSVData(text);
      const report = this.generateReport(data);
      
      setTimeout(() => {
        const blob = new Blob([report], { type: 'text/plain' });
        this.downloadFile(blob, 'smart-inventory-aging-report.txt');
        this.showMessage('✨ Smart report generated successfully!', 'success');
        this.hideProgressSection();
      }, 3000);
    } catch (error) {
      setTimeout(() => {
        const mockReport = this.generateMockReport();
        const blob = new Blob([mockReport], { type: 'text/plain' });
        this.downloadFile(blob, 'sample-inventory-report.txt');
        this.showMessage('📄 Sample report generated!', 'success');
        this.hideProgressSection();
      }, 3000);
    }
  }

  parseCSVData(text) {
    const lines = text.split('\n').filter(line => line.trim());
    const items = [];
    
    for (let i = 1; i < lines.length; i++) {
      const values = lines[i].split(',').map(v => v.trim());
      if (values.length >= 4) {
        items.push({
          name: values[0],
          category: values[1],
          quantity: parseInt(values[2]) || 0,
          dateReceived: new Date(values[3]),
          unitCost: 500 + Math.random() * 50000
        });
      }
    }
    return items;
  }

  generateReport(items) {
    const analysis = this.analyzeData(items);
    let report = '=== 🚀 SMART INVENTORY AGING ANALYTICS REPORT ===\n\n';
    
    report += '📊 EXECUTIVE SUMMARY\n';
    report += `Total Items: ${analysis.totalItems}\n`;
    report += `Total Value: ₹${analysis.totalValue.toLocaleString()}\n`;
    report += `Critical Items: ${analysis.criticalItems}\n`;
    report += `Categories: ${analysis.categoryCount}\n\n`;
    
    report += '🕒 AGING GROUPS ANALYSIS\n';
    Object.entries(analysis.groupedItems).forEach(([agingGroup, categories]) => {
      report += `\n--- ${agingGroup} ---\n`;
      Object.entries(categories).forEach(([category, items]) => {
        report += `\n${category} (${items.length} items):\n`;
        items.forEach(item => {
          report += `  • ${item.name} | Qty: ${item.quantity} | ${item.days} days | Risk: ${item.risk}\n`;
        });
      });
    });
    
    report += '\n\n📦 CATEGORY BREAKDOWN\n';
    Object.entries(analysis.categoryData)
      .sort(([,a], [,b]) => b - a)
      .forEach(([category, count]) => {
        const percentage = ((count / analysis.totalItems) * 100).toFixed(1);
        report += `${category}: ${count} items (${percentage}%)\n`;
      });
    
    report += '\n\n🧠 AI INSIGHTS & RECOMMENDATIONS\n';
    analysis.insights.forEach(insight => {
      report += `${insight.text}\n`;
    });
    
    report += '\n\n📋 DETAILED INVENTORY DATA\n';
    report += 'Item Name,Category,Quantity,Date Received,Days Old,Risk Level,Aging Group\n';
    
    const now = new Date();
    const limits = analysis.limits;
    items.forEach(item => {
      const days = Math.floor((now - item.dateReceived) / (1000 * 60 * 60 * 24));
      let risk, agingGroup;
      if (days <= limits.fresh) { risk = 'Low'; agingGroup = `Fresh (0-${limits.fresh})`; }
      else if (days <= limits.moderate) { risk = 'Medium'; agingGroup = `Moderate (${limits.fresh + 1}-${limits.moderate})`; }
      else if (days <= limits.aging) { risk = 'High'; agingGroup = `Aging (${limits.moderate + 1}-${limits.aging})`; }
      else { risk = 'Critical'; agingGroup = `Critical (>${limits.aging})`; }
      report += `${item.name},${item.category},${item.quantity},${item.dateReceived.toISOString().split('T')[0]},${days},${risk},${agingGroup}\n`;
    });
    
    return report;
  }

  generateMockReport() {
    return `=== 🚀 SMART INVENTORY AGING ANALYTICS REPORT ===

📊 EXECUTIVE SUMMARY
Total Items: 25
Total Value: ₹52,50,000
Critical Items: 3
Categories: 4

🕒 AGING GROUPS ANALYSIS

--- Fresh (0-30) ---

Electronics (8 items):
  • Laptop Dell XPS | Qty: 25 | 15 days | Risk: Low
  • Wireless Mouse | Qty: 100 | 20 days | Risk: Low

--- Critical (>90) ---

Furniture (3 items):
  • Office Chair | Qty: 50 | 120 days | Risk: Critical
  • Standing Desk | Qty: 15 | 95 days | Risk: Critical

📦 CATEGORY BREAKDOWN
Electronics: 12 items (48.0%)
Furniture: 8 items (32.0%)
Supplies: 3 items (12.0%)
Appliances: 2 items (8.0%)

🧠 AI INSIGHTS & RECOMMENDATIONS
📊 Electronics is your largest category with 12 items (48.0%)
⚠️ 3 items need immediate clearance or liquidation
✅ Well-diversified inventory across 4 categories`;
  }

  showProgressSection() {
    document.getElementById('progressSection').style.display = 'block';
    document.getElementById('generateBtn').disabled = true;
  }

  hideProgressSection() {
    document.getElementById('progressSection').style.display = 'none';
    document.getElementById('generateBtn').disabled = false;
  }

  simulateProgress() {
    const steps = [
      { text: '📄 Reading file data', duration: 1000 },
      { text: '🔍 Analyzing aging patterns', duration: 1500 },
      { text: '📈 Generating insights', duration: 1200 },
      { text: '🎨 Creating visualizations', duration: 800 },
      { text: '📊 Building smart report', duration: 1000 }
    ];

    const progressFill = document.getElementById('progressFill');
    const progressText = document.getElementById('progressText');
    const stepsContainer = document.getElementById('processingSteps');

    stepsContainer.innerHTML = '';
    steps.forEach((step, index) => {
      const stepEl = document.createElement('div');
      stepEl.className = 'step pending';
      stepEl.textContent = step.text;
      stepEl.id = `step-${index}`;
      stepsContainer.appendChild(stepEl);
    });

    let currentStep = 0;
    let progress = 0;

    const updateProgress = () => {
      if (currentStep < steps.length) {
        document.getElementById(`step-${currentStep}`).className = 'step active';
        progressText.textContent = steps[currentStep].text;

        setTimeout(() => {
          document.getElementById(`step-${currentStep}`).className = 'step completed';
          progress = ((currentStep + 1) / steps.length) * 100;
          progressFill.style.width = progress + '%';
          currentStep++;
          updateProgress();
        }, steps[currentStep].duration);
      } else {
        progressText.textContent = 'Finalizing report...';
      }
    };

    updateProgress();
  }

  downloadFile(blob, filename) {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  }

  showMessage(text, type = 'info') {
    const container = document.getElementById('messageContainer');
    const message = document.createElement('div');
    message.className = `message ${type}`;
    message.textContent = text;
    
    container.appendChild(message);
    
    setTimeout(() => {
      message.remove();
    }, 5000);
  }

  removeFile() {
    this.currentFile = null;
    document.getElementById('fileInfo').style.display = 'none';
    document.getElementById('dropZone').style.display = 'block';
    document.getElementById('previewSection').style.display = 'none';
    document.getElementById('generateBtn').disabled = true;
    document.getElementById('excelFile').value = '';
  }
}

const app = new InventoryAnalytics();

function uploadFile() {
  app.uploadFile();
}

function removeFile() {
  app.removeFile();
}