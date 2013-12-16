package fr.ece.neolog;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.jsoup.Jsoup;


public class Spooler {
	/**
	 * @param args : arg[0] = input file
	 */
	private static HTable hTable = null;
	
	public static void main(String[] args) throws Exception {
		Path input = new Path((args.length>1) ? args[1] : "input");
		
		System.out.println("*** INTPUT PATH: "+input.toUri().toString());
		
		Configuration conf = new Configuration();
		HBaseAdmin hbase = new HBaseAdmin(conf);
		HTableDescriptor desc = new HTableDescriptor("TEST");
		System.out.println("*** Creating HBase Configuration:");
		Configuration hConf = HBaseConfiguration.create();
		
		System.out.println("*** Opening HTABLE :");
		hTable = new HTable(hConf,(args.length>0) ? args[0] : "dictionnary");
		System.out.println("*** Creating Job");
		Job job = new Job(conf, "neolog");
		job.setMapperClass(Map.class);
 		job.setJarByClass(Spooler.class);
 		
 		//On a pas besoin de la clé de sortie, donc on met null
	 	job.setOutputKeyClass(NullWritable.class);
		job.setOutputValueClass(Text.class);
 		job.setNumReduceTasks(0);
		
 		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		FileInputFormat.addInputPath(job, input);
		FileOutputFormat.setOutputPath(job, new Path("output/"+System.currentTimeMillis()));
		job.waitForCompletion(true);
		
		try {
			FileSystem.get(conf).delete(input, true);
		}
		catch(IOException ex) {
			System.out.println(ex.getMessage());
		}
		
	}
	
	public static class Map extends Mapper<LongWritable, Text, Text, NullWritable> {
		 
	    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
	    	
	    	StringTokenizer text = new StringTokenizer(Jsoup.parse(value.toString()).select("body").text());
	    	while(text.hasMoreTokens()){
	    		checkHBaseTokenList(text.nextToken());
	    	}
    	}
	    private void checkHBaseTokenList(String str){
	    	System.out.println("TOKEN :" + str);
	    }
	}
}