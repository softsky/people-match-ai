package com.localblox.ashfaq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

import java.net.URISyntaxException;

import com.localblox.ashfaq.data.ProfileEnumerator;
import com.localblox.ashfaq.data.PersonProfile;

import org.bson.Document;
public class App {
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

	public static void main(String[] args) throws URISyntaxException {
		new App().proceed(args);
	}
	private void printUsage(PrintStream s) {
		final String url =
			"https://storage.googleapis.com/download.tensorflow.org/models/inception5h.zip";
		s.println(
		          "Java program that uses a pre-trained Inception model (http://arxiv.org/abs/1512.00567)");
		s.println("to label JPEG images.");
		s.println("TensorFlow version: " + TensorFlow.version());
		s.println();
		s.println("Usage: mvn exec:java -D<image file>");
		s.println();
		s.println("Where:");
		s.println("<image file> is the path to a JPEG image file");
	}	

	public void proceed(final String[] args) throws URISyntaxException {
		if(args.length == 0){
			this.printUsage(System.err);
		} else {
			final ProfileEnumerator profileEnumerator = new ProfileEnumerator();
			profileEnumerator.fetchAll();

			/**
			   IMPORTANT:
			   Basically since we haven't trained any model yet, we can't use actual model with our profiles.
			   This code just goes through all items returned from database, but emulate profile testing with
			   another AI operation (image recognition) to show the delay.
			   Since millions of profiles should be scanned to find best matches
			   this block should be i) multithreaded ii) run on many shards (clustered) iii) highly optimized
			   Screen output is very slow (especially on big monitors), you can redirect it to file and it will be much faster.
			   But slow for millions anyways.
			   Of course we would use other type of NN which returns several results at once. This will sped up things. But proposed 
			   system architecture is necessary anyways
			 */
			while(profileEnumerator.hasMoreElements()){			
				final String imageFile = args[0];
				final LabelProfiles lp = new LabelProfiles();
				final ClassLoader loader = LabelProfiles.class.getClassLoader();

				final byte[] graphDef = lp.readAllBytesOrExit(Paths.get(loader.getResource("model/tensorflow_inception_graph.pb").toURI()));
				final List<String> labels =
					lp.readAllLinesOrExit(Paths.get(loader.getResource("model/imagenet_comp_graph_label_strings.txt").toURI()));
				final byte[] imageBytes = lp.readAllBytesOrExit(Paths.get(imageFile));

				try (Tensor image = lp.constructAndExecuteGraphToNormalizeImage(imageBytes)) {
					float[] labelProbabilities = lp.executeInceptionGraph(graphDef, image);
					int bestLabelIdx = lp.maxIndex(labelProbabilities);
					System.out.println(
					                   String.format(
					                                 "BEST MATCH: %s %s (%.2f%% likely)",
					                                 Document.parse(profileEnumerator.nextElement().toString()).getObjectId("_id"),
					                                 labels.get(bestLabelIdx),
					                                 labelProbabilities[bestLabelIdx] * 100f));
				}
			}
		}
	}
		
}
