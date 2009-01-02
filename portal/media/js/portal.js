var PortalPage = function(config) {
	this.config = config;
	this.init(this);
}

PortalPage.prototype = {

	init: function() {
		//Not used at the moment - could be used to make portlets stay where the user puts them.
		//Ext.state.Manager.setProvider(new Ext.state.CookieProvider());

		var webcamsColumn = new Ext.ux.PortalColumn({
			id: 'webcams-column',
			columnWidth:.33,
			style:'padding:10px 0 10px 10px'
		});

		for (var i = 0; i < this.config.webcams.length; i++) {
			webcamsColumn.add({
				title: this.config.webcams[i].name,
				html: "<div class='webcam'><img src='" + this.config.webcams[i].url + "' alt='Webcam feed from " +
					  this.config.webcams[i].url + "' /></div>"
			});
		}

		var imageColumn = new Ext.ux.PortalColumn({
			columnWidth:.33,
			style:'padding:10px 0 10px 10px'
		});

		var projPanel = new Ext.ux.Portlet({
			xtype: 'portlet',
			id: 'proj-info',
			title: 'Project Information',
			collapsible: true,
			titleCollapse: false,
			height: 'auto',
			autoLoad: this.config.projInfo
		});
		imageColumn.add(projPanel);

		var latestCapturePanel = new Ext.ux.Portlet({
			xtype: 'portlet',
			id: 'latest-capture',
			title: 'Latest Capture',
			collapsible: true,
			titleCollapse: false,
			height: 267,
			autoLoad: this.config.captureImageUrl
		});
		imageColumn.add(latestCapturePanel);

		var sensorsColumn = new Ext.ux.PortalColumn({
			id: 'sensors-column',
			columnWidth:.33,
			style:'padding:10px 0 10px 10px'
		});


		var sensorTPanel = new Ext.ux.Portlet({
			xtype: 'portlet',
			id: 'curr-sensor',
			title: 'Current Sensor Readings',
			collapsible: true,
			titleCollapse: false,
			height: 'auto',
			autoLoad: this.config.currSensor
		});
		sensorsColumn.add(sensorTPanel);

		var sensorReadingsPanel = new Ext.ux.Portlet({
			xtype: 'portlet',
			id: 'sensor-readings',
			title: 'Graph - Sensor Readings',
			collapsible: true,
			titleCollapse: false,
			height: 'auto',
			autoLoad: this.config.sensorData
		});
		sensorsColumn.add(sensorReadingsPanel);

		var header = new Ext.Panel({
			contentEl: 'header',
			region: 'north',
			height: 67,
			frame: true,
			style: 'text-align: center'
		});

		var portal = new Ext.ux.Portal({
			region: 'center',
			margins:'5 5 5 5',
			items: [
					imageColumn,
					webcamsColumn,
					sensorsColumn
				]
		});

		var footer = new Ext.Panel({
			contentEl: 'footer',
			region: 'south',
			height: 55,
			frame: true,
			style: 'text-align: center'
		});

		var viewport = new Ext.Viewport({
			layout: 'border',
			items: [
					header,
					portal,
					footer
				]
		});
		viewport.doLayout();

		var pu = projPanel.getUpdater();
		pu.defaultUrl = this.config.projInfo;
		pu.showLoadIndicator = false;
		pu.startAutoRefresh(5);

		var cu = sensorTPanel.getUpdater();
		cu.defaultUrl = this.config.currSensor;
		cu.showLoadIndicator = false;
		cu.startAutoRefresh(5);

		var ru = sensorReadingsPanel.getUpdater();
		ru.defaultUrl = this.config.sensorData;
		ru.showLoadIndicator = false;
		ru.startAutoRefresh(5);

		var cu = latestCapturePanel.getUpdater();
		cu.defaultUrl = this.config.captureImageUrl;
		cu.showLoadIndicator = false;
		cu.disableCaching = false;
//		cu.disableCaching = true;
		cu.startAutoRefresh(5);
	}
};
