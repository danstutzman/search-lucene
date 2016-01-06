package org.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.json.JSONArray;
import org.json.JSONObject;
 
public class LuceneExample {
  public static void main(String[] args) throws CorruptIndexException,
      LockObtainFailedException, IOException {
  if (args.length < 1) {
     System.err.println("First argument: location of .json file");
     System.exit(1);
  }
  File jsonFile = new File(args[0]);

  if (args.length < 2) {
     System.err.println("Second argument: directory to build Lucene index in");
     System.exit(1);
  }
  File indexFile = new File(args[1]);

  Directory indexDirectory = FSDirectory.open(indexFile.toPath());
  StandardAnalyzer analyzer = new StandardAnalyzer();
  IndexWriterConfig config = new IndexWriterConfig(analyzer);
  IndexWriter indexWriter = new IndexWriter(indexDirectory, config);

  BufferedReader reader = new BufferedReader(new FileReader(jsonFile));
  String line;
  while ((line = reader.readLine()) != null) {
    JSONObject object = new JSONObject(line);
    if (object.getString("type").equals("song_text")) {
      JSONArray songTextLines = object.getJSONArray("song_text");
      for (int i = 0; i < songTextLines.length(); i++) {
        String songTextLine = songTextLines.getString(i);
        Document doc = new Document();
        //doc.add(new Field("body", songTextLine,
        //  Field.Store.YES, Field.Index.ANALYZED));
        StringField bodyField = new StringField("body", songTextLine, Field.Store.YES);
        doc.add(bodyField);
        indexWriter.addDocument(doc);
      }
    }
  }
  indexWriter.close();

 
   /*
        Query q = new MultiFieldQueryParser(new String[]{"title", "name"},
                analyzer).parse("s*");
 
        // searching ...
        int hitsPerPage = 10;
        IndexSearcher searcher = new IndexSearcher(index);
        TopDocCollector collector = new TopDocCollector(hitsPerPage);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
 
        // output results
        System.out.println("Found " + hits.length + " hits.");
        for (int i = 0; i < hits.length; ++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("name") + ": "
                    + d.get("title"));
        }
        */
 
    }
}
