package utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.stream.Collectors;

public class StopWordsChecker
{
	private HashSet<String> stopWords;

	public StopWordsChecker()
	{		
		try {
			FileWriter writer = new FileWriter("test.txt");
			writer.close();

			BufferedReader reader = new BufferedReader(new FileReader("resources/stopwords.txt"));
			stopWords = reader.lines().collect(Collectors.toCollection(HashSet::new));
			reader.close();
		} catch(Exception exception) {
			System.err.println(exception.toString());
		}
	}

    public boolean isStopWord(String str)
	{
		return stopWords.contains(str);	
	}
}

