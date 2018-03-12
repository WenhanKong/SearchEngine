

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.sql.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class serachEngineServlet
 */
public class SearchEngineServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchEngineServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String loginUser = "mytestuser";
        String loginPasswd = "mypassword";
        String loginUrl = "jdbc:mysql://localhost:3306/search?useSSL=false&autoReconnect=true";
        
        response.setContentType("text/json");
		JsonArray jsonArray = new JsonArray();
		
 		String query = request.getParameter("query").toLowerCase();
//		String query = "intelligence test computer science";
		if (query == null || query.trim().isEmpty()) {
			response.getWriter().write(jsonArray.toString());
			return;
		}
		
		String[] words = query.split(" ");
			
		int query_length = query.length();
		int typo_number = query_length/3;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select doc_id from term_id_tf_tfidf where term_id = ? ");
		for(int i = 1; i<words.length; i++) {
			sb.append("and doc_id in (select doc_id from term_id_tf_tfidf where term_id = ? ");
		}
		for(int i = 1; i<words.length; i++) {
			sb.append(")");
		}
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance(); 
	        	Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
	        	
	        	PreparedStatement ps = dbcon.prepareStatement(sb.toString());
	        	for(int i = 1; i<words.length+1; i++) {
	        		ps.setString(i, words[i-1]);
	        	}
	        	
	        	System.out.println(ps.toString());
	        	response.getWriter().append(ps.toString());
	        	ResultSet rs = ps.executeQuery();
	        	
	        	while(rs.next()) {
	        		float total_tfidf = 0;
	        		String doc_id = rs.getString(1);
	        		String url_string = "";
	        		String title = doc_id;
	        		String snippet = "";
	        		
	        		for(int i = 0; i<words.length; i++) {
	        			String s = "select tfidf from term_id_tf_tfidf where term_id = ? and doc_id = ?";
	        			PreparedStatement ps2 = dbcon.prepareStatement(s);
	        			ps2.setString(1, words[i]);
	        			ps2.setString(2, doc_id);
	        			
//	        			System.out.println("searching for term and doc: "+words[i]+rs.getString(1));
	        			
	        			ResultSet tfidf = ps2.executeQuery();
	        			if(tfidf.next()) {
//	        				System.out.println("tfidf for this terms is "+ tfidf.getFloat(1));
	        				total_tfidf += tfidf.getFloat(1);
//	        				System.out.println("total tfidf after add is " + total_tfidf);
	        			}
	        		}
//	        		System.out.println("total tfidf is :"+ total_tfidf);
	        		String url_sql = "select url from doc_id_url where id = '" + rs.getString(1) + "'";
//	        		System.out.println(rs.getString(1));
	        		Statement s = dbcon.createStatement();
	        		ResultSet url = s.executeQuery(url_sql);
	        		if(url.next()) {
//	        			System.out.println(url.getString(1));
	        			url_string = url.getString(1);
	        			
	        			try {
	        				File input = new File("/Users/Wenhan/PycharmProjects/ics121/SearchEngine/WEBPAGES_RAW_ALL/"+doc_id);
		        			Document doc = Jsoup.parse(input, "UTF-8");
		        			title = doc.title();
		        			snippet = doc.body().text().substring(0, 150)+" ...";
		        			
//		        			System.out.println(title);
//		        			System.out.println(snippet);
	        			} catch (Exception e){
//	        				e.printStackTrace();
//	        				System.out.println("not a html");
	        			}
	        		}
	        		jsonArray.add(generateJsonObject(doc_id, url_string, title, snippet, total_tfidf));
	        	}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
//		System.out.println(jsonArray.toString());
		
		JsonArray sortedJsonArray = new JsonArray();
		ArrayList<JsonObject> jsonValues = new ArrayList<JsonObject>();
	    for (int i = 0; i < jsonArray.size(); i++) {
	        jsonValues.add(jsonArray.get(i).getAsJsonObject());
	    }
//	    
//		//override sort method to sort jsonArray according to tfidf
//		Collections.sort(jsonValues, new Comparator<JsonObject>() {
//			private static final String KEY_NAME = "total_tfidf";
//			
//			@Override
//			public int compare(JsonObject a, JsonObject b) {
//				float af = 0;
//				float bf = 0;
//				try {
//					af = a.get(KEY_NAME).getAsFloat();
//					bf = b.get(KEY_NAME).getAsFloat();	
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				return Float.compare(bf, af);
//			}
//			
//		});
//		
//		for(int i = 0; i<jsonArray.size(); i++) {
//			sortedJsonArray.add(jsonValues.get(i));
//		}
//		System.out.println(jsonValues.toString());
		
//		response.getWriter().append(sortedJsonArray.toString());
		
		request.getSession().setAttribute("result", jsonValues);
		RequestDispatcher rd = request.getRequestDispatcher("/searchResult.jsp");
		rd.forward(request, response);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	
	private static JsonObject generateJsonObject(String doc_id, String url, String title, String snippet, float total_tfidf) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("doc_id", doc_id);
		jsonObject.addProperty("total_tfidf", total_tfidf);
		jsonObject.addProperty("url", url);
		jsonObject.addProperty("title", title);
		jsonObject.addProperty("snippet", snippet);
		
		return jsonObject;
	}

}
