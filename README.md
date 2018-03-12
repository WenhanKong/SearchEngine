SearchEngine
Based on mysql and java dynamic web project. Lab for ICS121

This projects contains 37497 web files that downloaded previously in WEBPAGES_RAW_ALL; Based on index(tf, tfidf) stored in MySQL, it is easy to perform ranked information retrival.

How to use this engine?

0. make sure you have python2.7, java, apache-tomcat, and mysql installed.
1. shell> mysql -u mytestuser -p < create_searchdb.sql
2. shell> python2.7 MySQLInjection.py (this program takes nearly four hours. Be prepared.)
3. shell> open tomcat by entering "http://localhost:8080" in browser, click app manager, click choose file
4. deploy SearchEngineServer.war
5. click SearchEngineServer. The page should jump to http://localhost:8080/SearchEngineServer/
6. type anything you want
7. wait for you result
8. The project use datatable plug-in. So click "score" to get ranked web url.
