

import java.net.UnknownHostException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.QueryBuilder;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

public class MongoToMySql {
	
	void insert(String mysqltableName, int interval)
		throws Exception
	{
		
		Connection connect = null;
		Statement statement = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		
		Class.forName("com.mysql.jdbc.Driver");
		
		
		connect = (Connection) DriverManager.getConnection("jdbc:mysql://localhost/cmpe283?"
		              + "user=root&password=");
		
		statement = (Statement) connect.createStatement();
		
		resultSet = statement
		          .executeQuery("select * from cmpe283."+mysqltableName);
		
		preparedStatement = (PreparedStatement) connect
		          .prepareStatement("insert into  cmpe283."+mysqltableName+" values ( ?, ?, ?, ?, ?, ?, ?)");
		
		
		while (resultSet.next()) {
		     
			  String vmname = resultSet.getString("vmname");
			  
		    }
		
		System.out.println("***************************************************");
		
		MongoClient mongo = new MongoClient( "localhost" , 27017 );

		DB db = mongo.getDB("cmpe283");
		
		DBCollection collection = db.getCollection("metric");
		
		
		Date endDate;
        //Calendar cal1 = Calendar.getInstance(Locale.US);
		//Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		Calendar cal1 = Calendar.getInstance(Locale.US);
		//Convert to GMT
		//cal1.add(cal1.HOUR, 8);
        endDate = cal1.getTime();
        
		
		
        Date startDate;
   
        //Add 5 minutes interval
        cal1.add(cal1.SECOND, -interval);
        startDate = cal1.getTime();
        
        System.out.println("StartDate: " + startDate);
        System.out.println("EndDate: " + endDate);
        
	    BasicDBObject newquery = new BasicDBObject(); 
	    newquery.put("@timestamp", BasicDBObjectBuilder.start("$gte", startDate).add("$lte", endDate).get());
	    //newquery.put("@timestamp", "$lte", new Date()));
	    DBCursor cur = collection.find(newquery);         
	   
	    long recordCount =  cur.count();
	    System.out.println("count of records: " + recordCount);
		
		System.out.println("done");
		String vmname="";
		Float cpu_usage_perc=0.0f;
		Float mem_used_kb=0.0f;
		Float io_read_byte=0.0f;
		Float io_write_byte=0.0f;
		Float nw_tx_kbps=0.0f;
		//Add only the start date
		Timestamp t = new Timestamp(startDate.getTime());
		
		
		//Aggregate
		while(cur.hasNext())
		{
			DBObject doc = cur.next();
			vmname = doc.get("vmname").toString();
			//System.out.println(vmname);
			
			cpu_usage_perc += Float.parseFloat(doc.get("cpu_usage_perc").toString());
			//System.out.println("cpu_usage_perc:" + cpu_usage_perc);
			
			mem_used_kb += Float.parseFloat(doc.get("mem_used_kb").toString());
			//System.out.println("mem_used_kb:" + mem_used_kb);
			
			io_read_byte += Float.parseFloat(doc.get("io_read_byte").toString());
			//System.out.println("io_read_byte:" + io_read_byte);
			
			io_write_byte += Float.parseFloat(doc.get("io_write_byte").toString());
			//System.out.println("io_write_byte:" + io_write_byte);
			
			nw_tx_kbps += Float.parseFloat(doc.get("nw_tx_kbps").toString());
			//System.out.println("nw_tx_kbps:" + nw_tx_kbps);
		}	
			
		preparedStatement.setString(1, vmname);
	    preparedStatement.setDouble(2, cpu_usage_perc/recordCount);
	    preparedStatement.setDouble(3, mem_used_kb/recordCount);
	    preparedStatement.setDouble(4, io_read_byte/recordCount);
	    preparedStatement.setDouble(5, io_write_byte/recordCount);
	    preparedStatement.setDouble(6, nw_tx_kbps/recordCount);
	    preparedStatement.setTimestamp(7, t);
	    
	    preparedStatement.executeUpdate();
		
	}
	
	Thread minutesThread = new Thread(){
		public void run(){
			try{
				while(true){
					System.out.println("Inserting minutes data...");
					insert("Minutes", 300);
					Thread.sleep(300000);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	
	Thread hourThread = new Thread(){
		public void run(){
			try{
				while(true){
					System.out.println("Inserting hourly data...");
					insert("Hourly", 3600);
					Thread.sleep(3600000);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	Thread dailyThread = new Thread(){
		public void run(){
			try{
				while(true){
					System.out.println("Inserting daily data...");
					insert("Daily", 86400);
					Thread.sleep(86400000);
				}
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	};
	
	public static void main(String args[]) 
	{
		try{
			MongoToMySql m = new MongoToMySql();
			m.minutesThread.start();
			m.hourThread.start();
			m.dailyThread.start();
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}

}
