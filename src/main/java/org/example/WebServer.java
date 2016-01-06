package org.example;

import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.es.SpanishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class WebServer extends AbstractHandler {
  private static final int HITS_PER_PAGE = 100;

  private File indexFile;

  public WebServer(File indexFile) {
    this.indexFile = indexFile;
  }

  public void handle(String target, Request baseRequest,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {

    if (request.getPathInfo().equals("/")) {
      String queryParam = request.getParameter("query");
      if (queryParam != null) {
        IndexReader reader =
          DirectoryReader.open(FSDirectory.open(indexFile.toPath()));
        if (queryParam.equals("")) {
          for (int i = 0; i < reader.numDocs(); i++) {
            Document doc = reader.document(i);
            System.out.println(doc.get("body"));
          }
        } else {
          //StandardAnalyzer analyzer = new StandardAnalyzer();
          SpanishAnalyzer analyzer = new SpanishAnalyzer();

          //QueryParser parser = new QueryParser("body", analyzer);
          QueryParser parser = new MultiFieldQueryParser(
            new String[] {"song_name", "song_text"}, analyzer);
          Query query;
          try {
            query = parser.parse(queryParam);
          } catch (org.apache.lucene.queryparser.classic.ParseException e) {
            throw new RuntimeException(e);
          }

          IndexSearcher searcher = new IndexSearcher(reader);
          ScoreDoc[] hits = searcher.search(query, HITS_PER_PAGE).scoreDocs;

          response.setContentType("text/html;charset=utf-8");
          response.setStatus(HttpServletResponse.SC_OK);
          response.getWriter().println("<ul>");
          for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document doc = searcher.doc(docId);
            response.getWriter().println(
              "<li>" + doc.get("artist_name") + " - " +
              doc.get("song_name") + "</li>");
          }
          response.getWriter().println("</ul>");
        }
      } else { // if query is null
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(
          "<form> " +
          "<label>Query</label><br>" +
          "<input name='query'>" +
          "</form>"
        );
      }
    } else {
      response.setContentType("text/plain;charset=utf-8");
      response.setStatus(HttpServletResponse.SC_NOT_FOUND);
      response.getWriter().println("404 Not Found");
    }
    baseRequest.setHandled(true);
  }

  public static void main(String[] args) throws Exception, InterruptedException {
    if (args.length == 0) {
      System.err.println("First arg should be Lucene index directory");
      System.exit(1);
    }
    File indexFile = new File(args[0]);

    Server server = new Server(8080);
    WebServer handler = new WebServer(indexFile);
    server.setHandler(handler);
    server.start();
    server.join();
  }
}
