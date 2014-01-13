import json
import datetime
import bottle
import MySQLdb as mdb
import sys
from bottle import route, run, request, abort, template, response

 

 
@route('/data', method='GET')
def get_data():
	
	
	try:
		con = mdb.connect(host='130.65.133.152',port=3306,user='root',db= 'logstash')
		cur=con.cursor()
		myquery=''
		if len(request.query['vmsearch'])>0:
			myquery='select * from '+request.query['vmoption']+' where vmname="'+request.query['vmsearch']+'"  order by timestamp;'
		else:
			myquery='select * from '+request.query['vmoption']+'  order by vmname,timestamp;'
		print myquery	
		cur.execute(myquery)	
		results = cur.fetchall()
		myjson=[]
		vmjson=[]
		vmname=""
		print len(results)
		for row in results:
                        if vmname!=row[0]:
                                if vmname!="":
                                        templ=vmjson
					myjson.append(templ)
					vmjson=[]
				vmname=row[0]
				
			d={}	
			d["vm"]=row[0]
			d["vmtime"]=str(row[6])
			d["CPU"]=row[1]
			d["mem"]=row[2]
			d["net"]=row[5]
			d["ior"]=row[3]
			d["iow"]=row[4]
			vmjson.append(d)
			
		
		myjson.append(vmjson)	
		
		response.headers['Access-Control-Allow-Origin'] = '*'
		response.content_type = "application/json; charset=UTF-8"
		
		return json.dumps(myjson)
		
	except mdb.Error, e:
		print e
		

	
     

	
run(host='localhost', port=8088)
