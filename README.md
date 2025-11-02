# 🚀 Smart Inventory Aging Analytics

A modern, AI-powered inventory aging report generator that transforms your Excel data into actionable business insights.

## ✨ Unique Features

### 🎯 Smart Analytics
- **AI-Powered Recommendations** - Get intelligent suggestions based on your inventory patterns
- **Real-time Risk Assessment** - Automatic risk level calculation for each item
- **Multi-dimensional Analysis** - Category, supplier, and location-based insights

### 🎨 Modern Interface
- **Drag & Drop Upload** - Intuitive file handling with visual feedback
- **Real-time Progress Tracking** - See exactly what's happening during processing
- **Interactive Charts** - Beautiful visualizations powered by Chart.js
- **Responsive Design** - Works perfectly on all devices

### 📊 Advanced Reporting
- **Executive Summary** - High-level KPIs for management
- **Detailed Aging Analysis** - Comprehensive item-by-item breakdown
- **Category Analysis** - Value distribution across categories
- **AI Recommendations** - Actionable insights for inventory optimization

### 🔧 Technical Excellence
- **CORS Enabled** - Seamless frontend-backend communication
- **Error Handling** - Robust file validation and processing
- **Multiple Excel Formats** - Supports .xlsx and .xls files
- **Flexible Data Parsing** - Handles various date formats automatically

## 🚀 Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- Modern web browser

### Backend Setup
```bash
cd Backend
mvn clean compile
mvn exec:java -Dexec.mainClass="InventoryAgingReportGenerator"
```

### Frontend Setup
1. Open `Frontend/index.html` in your browser
2. Or serve with a local server:
```bash
cd Frontend
python -m http.server 8000
# Then visit http://localhost:8000
```

## 📋 Excel File Format

Your Excel file should have these columns (in order):
1. **Item Name** - Product/item identifier
2. **Category** - Product category
3. **Quantity** - Number of items
4. **Date Received** - When item was received (YYYY-MM-DD or MM/DD/YYYY)

### Sample Data
Use the provided `sample-inventory-data.csv` file to test the application.

## 🎯 How It Works

1. **Upload** - Drag and drop your Excel file or browse to select
2. **Configure** - Choose report options (charts, summary, recommendations)
3. **Process** - Watch real-time progress as your data is analyzed
4. **Download** - Get your comprehensive aging report with insights

## 📈 Report Sections

### Executive Summary
- Total items and value
- Critical aging statistics
- Key performance indicators
- Risk assessment overview

### Detailed Analysis
- Item-by-item aging breakdown
- Risk level classification
- Supplier and location tracking
- Cost and value calculations

### Category Analysis
- Value distribution by category
- Category-specific aging patterns
- Performance comparisons

### AI Recommendations
- Automated insights based on data patterns
- Actionable suggestions for inventory optimization
- Risk mitigation strategies
- Procurement recommendations

## 🔧 Customization

### Adding New Aging Buckets
Modify the `groupByAging()` method in the Java backend to add custom aging periods.

### Custom Styling
Update `style.css` to match your brand colors and design preferences.

### Additional Analytics
Extend the `InventoryAnalysis` class to include more sophisticated calculations.

## 🛠️ API Endpoints

- `POST /upload` - Upload Excel file and generate report
  - Form data: `file` (Excel file)
  - Options: `includeCharts`, `includeSummary`, `includeRecommendations`
  - Returns: Excel report file

## 🎨 Color Scheme

The application uses a modern gradient design:
- Primary: `#667eea` to `#764ba2`
- Success: `#48bb78`
- Warning: `#ed8936`
- Error: `#e53e3e`
- Critical: `#f56565`

## 📱 Browser Support

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 🔒 Security Features

- File type validation
- File size limits (10MB max)
- CORS protection
- Input sanitization
- Error boundary handling

## 🚀 Performance

- Optimized Excel processing
- Lazy loading for large datasets
- Efficient memory management
- Compressed response handling

## 📄 License

This project is open source and available under the MIT License.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 📞 Support

For issues or questions:
1. Check the sample data format
2. Verify Java and Maven versions
3. Ensure CORS is properly configured
4. Check browser console for errors

---

**Built with ❤️ using Java, Spark, Apache POI, HTML5, CSS3, and Chart.js**