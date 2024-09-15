package jeffgriffith.mnist;

import static java.lang.Math.min;
import static org.junit.Assert.*;

import java.io.*;
import java.util.List;

import org.junit.Test;

/**
 * @author haoxingxiao
 * Load the mnist images and labels,
 * then store the images as vectors in a new file.
 */
public class TestMnistReader {

    static final String IMAGE_FILE = "mnist_dataset" + File.separator + "train-images-idx3-ubyte";
    static final String LABEL_FILE = "mnist_dataset" + File.separator + "train-labels-idx1-ubyte";

    @Test
    public void test() throws IOException {
        String currentPath = System.getProperty("user.dir");
        String fullImagePath = currentPath + File.separator + IMAGE_FILE;
        String fullLabelPath = currentPath + File.separator + LABEL_FILE;

        String printPath = currentPath + File.separator + "mnist_dataset" + File.separator + "mnist_60000_784";

        int[] labels = MnistReader.getLabels(fullLabelPath);
        List<int[][]> images = MnistReader.getImages(fullImagePath);

        assertEquals(labels.length, images.size());
        assertEquals(28, images.get(0).length);
        assertEquals(28, images.get(0)[0].length);

        // get top 10 samples
        for (int i = 0; i < min(10, labels.length); i++) {
            System.out.printf("================= LABEL %d\n", labels[i]);
            System.out.printf("%s", MnistReader.renderImage(images.get(i)));
        }
        //write into the file
        BufferedWriter writer = new BufferedWriter(new FileWriter(printPath));
        int x = images.size();
        int y = images.get(0).length;
        int z = images.get(0)[0].length;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                for (int k = 0; k < z; k++) {
                    writer.write(images.get(i)[j][k] + " ");
                }
            }
            writer.write('\n');
        }
    }
}
