package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
 
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
 
public class LuceneExample {
 
    public static void main(String[] args) throws CorruptIndexException,
            LockObtainFailedException, IOException, ParseException {
        List<String> l = new ArrayList<String>();
        l.add("you all");
        l.add("visit");
        l.add("some blog");
        l.add("sometimes");
 
        // create some index
        // we could also create an index in our ram ...
        // Directory index = new RAMDirectory();
        Directory index = FSDirectory.getDirectory("/tmp/ourtestindex/");
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriter w = new IndexWriter(index, analyzer, true,
                IndexWriter.MaxFieldLength.UNLIMITED);
 
        // index some data
        for (String i : l) {
            System.out.println("indexing " + i);
            Document doc = new Document();
            doc.add(new Field("title", i, Field.Store.YES,
                            Field.Index.ANALYZED));
            doc.add(new Field("name", i, Field.Store.YES,
                            Field.Index.ANALYZED));
            w.addDocument(doc);
        }
 
        // loop and index some random data
        for (int i = 1; i < 40000; i++) {
            Document doc = new Document();
            doc.add(new Field("title", "xyz" + i, Field.Store.YES,
                    Field.Index.ANALYZED));
            doc.add(new Field("name", "" + i, Field.Store.YES,
                    Field.Index.ANALYZED));
            w.addDocument(doc);
        }
        w.close();
        System.out.println("index generated");
        // parse query over multiple fields
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
 
    }
}
