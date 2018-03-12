<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ page language="java"
	import="java.sql.*, 
	java.util.*,
	java.io.IOException,
	javax.servlet.http.*,
	javax.servlet.*,
	com.google.gson.JsonArray,
	com.google.gson.JsonObject"%>
		

<% 
List<JsonObject> result = (List<JsonObject>)session.getAttribute("result");
System.out.println(result);

%>

<!DOCTYPE html >
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>ICS Search Result</title>
<link rel="stylesheet" href="https://cdn.datatables.net/1.10.16/css/jquery.dataTables.min.css">
	  <script type="text/javascript" charset="utf8" src="http://ajax.aspnetcdn.com/ajax/jQuery/jquery-1.8.2.min.js"></script>
	  <script type="text/javascript" charset="utf8" src="http://ajax.aspnetcdn.com/ajax/jquery.dataTables/1.9.4/jquery.dataTables.min.js"></script>
	  <script type="text/javascript">
	        $(document).ready(function () {
	            $("#movieTable").dataTable({
              	 "sPaginationType":"full_numbers", 
	
	            
	            });
	        });

	  </script>
  <style>
  table {
  	margin: 0 auto;
  	table-layout: fixed;
  	word-wrap:break-word;
  }
  </style>
</head>
<body>
	<table id = "movieTable" class="display" cellspacing="0" width="100%">
	    <thead>
			<tr>
				<th>
					Title
				</th>
				<th>Score</th>
				<th>Snippet</th>
				<th>docID</th>
				<th>url</th>
				
			</tr>
		</thead>
		<tbody>
			<% 
				for (JsonObject term:result) {
			%>
			<tr>
				<td><%=term.get("title").getAsString() %></td>
				<td><%=term.get("total_tfidf").getAsFloat() %>
				<td><%=term.get("snippet").getAsString() %></td>
				
				<td><%=term.get("doc_id").getAsString() %></td>
				<td><a href="http://<%=term.get("url").getAsString() %>"><%=term.get("url").getAsString() %></a></td>
			</tr>
			<%
			  }
			%>
		</tbody>
		</table>

</body>
</html>
</html>