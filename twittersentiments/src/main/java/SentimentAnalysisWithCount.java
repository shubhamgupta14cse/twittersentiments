import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;


public class SentimentAnalysisWithCount {

    DoccatModel model;
    private static int positive = 0;
    private static int negative = 0;
    private static String ConsumerKey= "PUT CONSUMER KEY HERE";
    private static String ConsumerSecret= "PUT CONSUMER SECRET HERE";
    private static String AccessToken= "PUT ACCESS TOKEN HERE";
    private static String AccessTokenSecret= "PUT ACCESS TOKEN SECRET HERE";

    public static void main(String[] args) throws IOException, TwitterException {
        SentimentAnalysisWithCount twitterCategorizer = new SentimentAnalysisWithCount();
        twitterCategorizer.trainModel();

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(ConsumerKey)
                .setOAuthConsumerSecret(ConsumerSecret)
                .setOAuthAccessToken(AccessToken)
                .setOAuthAccessTokenSecret(AccessTokenSecret);
        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twitter = tf.getInstance();
        Query query = new Query("tiger zinda hai");
        query.setCount(50);
        QueryResult result = twitter.search(query);
        int result1 = 0;
        for (Status status : result.getTweets()) {
            result1 = twitterCategorizer.classifyNewTweet(status.getText());
            if (result1 == 1) {
                positive++;
            } else {
                negative++;
            }
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter("C:\\Users\\Student\\Desktop\\results.csv")); // Path to the results file
        bw.write("Positive Tweets," + positive);
        bw.newLine();
        bw.write("Negative Tweets," + negative);
        bw.close();
    }

    public void trainModel() {
        InputStream dataIn = null;
        try {
            dataIn = new FileInputStream("C:\\Users\\Student\\Desktop\\tweets.txt"); // Path to dataset
            ObjectStream lineStream = new PlainTextByLineStream(dataIn, "UTF-8");
            ObjectStream sampleStream = new DocumentSampleStream(lineStream);
            // Specifies the minimum number of times a feature must be seen
            int cutoff = 2;
            int trainingIterations = 30;
            model = DocumentCategorizerME.train("en", sampleStream, cutoff,
                    trainingIterations);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (dataIn != null) {
                try {
                    dataIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int classifyNewTweet(String tweet) throws IOException {
        DocumentCategorizerME myCategorizer = new DocumentCategorizerME(model);
        double[] outcomes = myCategorizer.categorize(tweet);
        String category = myCategorizer.getBestCategory(outcomes);

        System.out.print("-----------------------------------------------------\nTWEET :" + tweet + " ===> ");
        if (category.equalsIgnoreCase("1")) {
            System.out.println(" POSITIVE ");
            return 1;
        } else {
            System.out.println(" NEGATIVE ");
            return 0;
        }

    }
}