package org.hadian.bagraph.generators;

import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

/**
 * Created by ali on 18/01/17.
 */
public class FigureGenerator {
    private final static int NUM_TESTS = 10;
    private final static String[] columns = {"SamplingMode", "NumNodes", "m", "NumEdges", "NumComparisons", "TotalTime", "SamplingTime", "MaintenanceTime", "NumBuckets", "TreeCodeWordLength", "TreeOptimalHuffmanCodeWordLength", "TotalBucketsInserted", "TotalBucketsRemoved"};
    private final static int MAX_COLUMNS = columns.length;
    private static List<String> baseParams = Arrays.asList(new String[]{"java", "-Xmx4g", "-jar", "target/ROLL-0.3-SNAPSHOT-jar-with-dependencies.jar"});
    private static final DecimalFormat df = new DecimalFormat("#.0000000");
    private static String FIGURES_PARAM_PREFIX = "-fig";

    public static void main(String args[]) throws Exception {
        FigureGenerator figGen = new FigureGenerator();
        for (String arg : args) {
            if (arg.startsWith("-Xmx"))
                baseParams.set(1, arg);
            if (arg.toLowerCase().startsWith(FIGURES_PARAM_PREFIX)) {
                String figNumStr = arg.substring(FIGURES_PARAM_PREFIX.length());
                if (!NumberUtils.isDigits(figNumStr))
                    throw new Exception("Cannot parse parameter: " + arg);
                if (Arrays.stream(FigureGenerator.class.getDeclaredMethods()).map(method -> method.getName()).noneMatch(m -> m.equals("runFig" + figNumStr)))
                    throw new Exception("Do not have such figure: " + arg);

                //for figNum=X, run function runFigX
                java.lang.reflect.Method method = FigureGenerator.class.getMethod("runFig" + figNumStr);
                method.invoke(null);
            }
        }

    }

    public static void runFig2() throws IOException, InterruptedException {
        System.out.println("\n\n    Fig 2    \n===============================");
        for (int np = 3; np <= 9; np++) {
            for (int m : new int[]{2, 10, 20}) {
                String n = String.valueOf((long) Math.pow(10, np));
                Map<String, Double> resultRollBucket = testAndAverage("-s", "roll-tree", "-n", n, "-m", String.valueOf(m));
                double ratio = 100 * (1 - (resultRollBucket.get("NumBuckets") / resultRollBucket.get("NumNodes")));
                persist("data.red.m" + m + ".txt", n, df.format(ratio));
            }
        }
    }

    public static void runFig3() throws IOException, InterruptedException {
        System.out.println("\n\n    Fig 3    \n===============================");
        for (int np = 3; np <= 8; np++) {
            for (int m : new int[]{2, 10, 20}) {
                String n = String.valueOf((long) Math.pow(10, np));
                Map<String, Double> resultRollBucket = testAndAverage("-s", "roll-bucket", "-n", n, "-m", String.valueOf(m));
                persist("data.nComp.RWB.m" + m + ".txt", n, df.format(resultRollBucket.get("NumComparisons")));
                persist("data.e1.RWB.m" + m + ".txt", n, df.format(resultRollBucket.get("TotalTime") / Math.pow(10, 9)));
                resultRollBucket = testAndAverage("-s", "roll-bucket-sorted", "-n", n, "-m", String.valueOf(m));
                persist("data.nComp.RWB_Sorted.m" + m + ".txt", n, df.format(resultRollBucket.get("NumComparisons")));
                persist("data.e1.RWB_Sorted.m" + m + ".txt", n, df.format(resultRollBucket.get("TotalTime") / Math.pow(10, 9)));
            }
        }
    }


    public static void runFig5() throws IOException, InterruptedException {
        System.out.println("\n\n    Fig 5    \n===============================");
        for (int np = 3; np <= 9; np++) {
            for (int m : new int[]{2, 10, 20}) {
                String n = String.valueOf((long) Math.pow(10, np));
                Map<String, Double> resultRollTree = testAndAverage("-s", "roll-tree", "-n", n, "-m", String.valueOf(m));
                persist("data.cwl.Huffman.m" + m + ".txt", n, df.format(resultRollTree.get("TreeOptimalHuffmanCodeWordLength")));
                persist("data.cwl.RWBT.m" + m + ".txt", n, df.format(resultRollTree.get("TreeCodeWordLength")));
            }
        }
    }


    public static void runFig6() throws IOException, InterruptedException {
        System.out.println("\n\n    Fig 6    \n===============================");
        for (int np = 3; np <= 8; np++) {
            for (int m : new int[]{2, 10, 20}) {
                String n = String.valueOf((long) Math.pow(10, np));
                Map<String, Double> resultRollTree = testAndAverage("-s", "roll-tree", "-n", n, "-m", String.valueOf(m));
                Map<String, Double> resultRollTreeReduced = testAndAverage("-s", "roll-tree-reduced", "-n", n, "-m", String.valueOf(m));
                double rollTreeOperations = resultRollTree.get("TotalBucketsInserted") + resultRollTree.get("TotalBucketsRemoved");
                double rollTreeReducedOperations = resultRollTreeReduced.get("TotalBucketsInserted") + resultRollTreeReduced.get("TotalBucketsRemoved");
                double reductionRatio = 100 * (1 - (rollTreeReducedOperations / rollTreeOperations));
                persist("data.ins_reduce.m" + m + ".txt", n, df.format(reductionRatio));
            }
        }
    }

    public static void runFig7() throws IOException, InterruptedException {
        System.out.println("\n\n    Fig 7    \n===============================");
        for (int np = 3; np <= 8; np++) {
            for (int m : new int[]{2, 10, 20}) {
                String n = String.valueOf((long) Math.pow(10, np));
                Map<String, Double> result = testAndAverage("-s", "roll-tree", "-n", n, "-m", String.valueOf(m));
                persist("data.BvsT.T.m" + m + ".txt", n, df.format(result.get("NumComparisons") / result.get("NumEdges")));
                result = testAndAverage("-s", "roll-bucket", "-n", n, "-m", String.valueOf(m));
                persist("data.BvsT.B.m" + m + ".txt", n, df.format(result.get("NumComparisons") / result.get("NumEdges")));
            }
        }
    }


    public static void runFig8() throws IOException, InterruptedException {
        System.out.println("\n\n    Fig 8    \n===============================");
        String n = String.valueOf((long) Math.pow(10, 9));
        String m = "2";
        ArrayList<String> fullParams = new ArrayList<String>(baseParams);
        fullParams.addAll(Arrays.asList(new String[]{"-s", "roll-tree", "-n", n, "-m", String.valueOf(m), "-d", "data.distribution.txt"}));
        System.err.println(fullParams.stream().reduce("\nParams: ", (a, b) -> a + " " + b));
        runTest(fullParams);
        //System.err.println("Extracting degree distribution...");

        /*Map<Long, Long> counts = Files.lines(Paths.get(HISTOGRAM_TEMP_FILE_NAME)).flatMap(l -> Arrays.stream(l.split("\t"))).map(s -> Long.parseLong(s)).collect(groupingBy(Function.identity(), counting()))
                .values().stream().collect(groupingBy(Function.identity(), counting()));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("data.distribution.txt"))) {
            for (long deg : counts.keySet())//.stream().sorted().collect(Collectors.toList()))
                writer.write(deg + "\t" + counts.get(deg) + "\n");
        }*/
    }


    public static void persist(String fileName, String key, String value) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true));
        System.out.printf("\t +Persisted: (%s,%s) ==> %s\n", key, value, fileName);
        bw.write(key + "\t" + value);
        bw.newLine();
        bw.flush();
        bw.close();
    }

    public static Map<String, Double> testAndAverage(String... params) throws IOException, InterruptedException {
        double[] sums = new double[MAX_COLUMNS];
        HashMap<String, Double> results = new HashMap<String, Double>();
        String[] res = null;

        ArrayList<String> fullParams = new ArrayList<String>(baseParams);
        for (String param : params) {
            fullParams.add(param);
        }
        System.out.println(Arrays.stream(params).reduce("\nParams: ", (a, b) -> a + " " + b));


        for (int i = 0; i < NUM_TESTS; i++) {
            res = runTest(fullParams);
            for (int col = 1; col < Math.min(MAX_COLUMNS, res.length); col++) {
                sums[col] += Double.parseDouble(res[col]);
            }
        }

        for (int col = 1; col < MAX_COLUMNS; col++)
            results.put(columns[col], sums[col] / NUM_TESTS);


        return results;
    }

    public static String[] runTest(List<String> params) throws IOException, InterruptedException {
        final ProcessBuilder builder = new ProcessBuilder(params);
        //"java", "-Xmx2g", "-jar", "target/ROLL-0.3-SNAPSHOT-jar-with-dependencies.jar", "-n", "1000000", "-m", "4", "-s", "roll-tree");
        Process process = builder.start();
        process.waitFor();
        String line = null;
        try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String lastLine;
            while ((lastLine = input.readLine()) != null) {
                System.err.println("\t-Output:" + lastLine);
                line = lastLine;
            }
        }
        //System.out.println("output: " + process.exitValue());
        if (process.exitValue() != 0)
            throw new IOException("\tcannot run the program for parameters: " + params.toString());

        if (line == null)
            throw new IOException("\toutput is null for parameters: " + params.toString());

        String[] outputList = line.split(" ");
        if (outputList.length < 11)
            throw new IOException("\toutput is not valid for parameters: " + params.toString() + "\n\n" + line);

        return outputList;
    }
}