This web application defines a servlet to return the following sensor infromation.
1. <context>/sensor.htm returns URL for sensor graph in minutes. Copy the returned value and paste it in your browser to view google chart for sensor.

2. <context>/sensor.htm?type=hour returns URL for sensor graph in hour. Copy the returned value and paste it in your browser to view google chart for sensor.

3. <context>/sensor.htm?type=current returns a html snippet with current sensor information.

Ensure that the URL to fetch sensor data (labjack or other source) is properly defined in webapp/WEB-INF/dimsimSensor-servlet.xml - parameter sensorBaseURL for bean SensorGraphController 
