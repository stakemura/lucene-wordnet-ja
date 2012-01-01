package synonym_generator;

import java.util.HashSet;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import com.google.common.base.Joiner;

/** 
 * Synonym Generator 
 * 
 * @author 
 *      Shintaro TAKEMURA (stakemura@gmail.com) 
 */  
public class App 
{
    @Option(name="-i", aliases="--input", usage="Japanese WordNet SQLite DB path")
    private String inputPath = "wnjpn.db";
	
    @Option(name="-o", aliases="--output", usage="Synonym file path")
    private String outputPath = "synonym_en_ja.txt";
	
	@Option(name="-h", aliases="--help", usage="print usage message and exit")
    private boolean usageFlag = false;
	
    public static void main( String[] args ) throws ClassNotFoundException
    {
    	App app = new App();

        CmdLineParser parser = new CmdLineParser(app);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException e) {
            e.printStackTrace();
            return;
        }
        
        if (app.usageFlag) {
        	System.out.println("Synonym generator");
            parser.printUsage(System.out);
            return;
        }
                
    	//System.out.println(System.getProperty("user.dir"));
    	
		// load the SQLite JDBC driver using the current class loader
		Class.forName("org.sqlite.JDBC");
		
		Connection connection = null;
		String db_url = "jdbc:sqlite:"+app.inputPath;

		try {
			// create a database connection
			connection = DriverManager.getConnection(db_url);

			Statement stat_synset = connection.createStatement();
			ResultSet _synset = stat_synset.executeQuery(
				"select * from synset");

			Statement stat_word = connection.createStatement();
			
			File outputFile = new File(app.outputPath);
			FileWriter wr = new FileWriter(outputFile , false);
			BufferedWriter bw = new BufferedWriter(wr);
			PrintWriter pw = new PrintWriter(bw);
			
			while (_synset.next()) {
				String synset = _synset.getString("synset");

				ResultSet _word = stat_word.executeQuery(
					"select word.lemma from word,sense where sense.synset=\"" + synset + "\" and word.wordid=sense.wordid");
				
				HashSet<String> lemmas = new HashSet<String>();
				while (_word.next()) {
					lemmas.add(_word.getString("lemma"));
				}
				
				if(lemmas.size()>=2)
				{
					pw.println(Joiner.on(',').join(lemmas).replace('_',' '));
				}
				
				_word.close();
			}
			
			_synset.close();

			connection.close();
			
			pw.close();
			
		}
		catch (IOException e) {
			// Failed to write file
			e.printStackTrace();
	    }
		catch (SQLException e) {
			// if the error message is "out of memory",
			// it probably means no database file is found
			System.out.println("Failed to connect "+db_url);
			e.printStackTrace();
		}
		finally {
		}
	}
}
