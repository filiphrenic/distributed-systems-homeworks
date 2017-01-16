package hr.fer.tel.rassus.pdq;

import com.perfdynamics.pdq.*;

import java.util.StringJoiner;

/**
 * @author Hruntek
 */
public class Lab {

	private static final double a = 0.3;
	private static final double b = 0.45;
	private static final double c = 0.3;

	private static final double[] S = new double[]{0.01, 0.05, 0.5, 0.2};

	public static void main(String[] args) {
		PDQ pdq = new PDQ();

		final String numFmt = "%.6f";
		final double[] v = new double[S.length];
		v[0] = (a - c) / (1 - c);
		v[1] = (1 - a) / (1 - c);
		v[2] = 1 / ((1 - b) * (1 - c));
		v[3] = 1 / (1 - c);


		for (int i = 0; i < 4; i++) {
			System.out.println(v[i]*S[i]);
		}

		StringJoiner header = new StringJoiner(";");
		header.add("Lambda");
		header.add("Total response time");
		System.out.println(header);

		double lambda = 0.5;
		pdq.Init("Treća domaća zadaća");
		pdq.CreateOpen("Zahtjevi", lambda);

		for (int i = 0; i < S.length; i++) {
			String name = String.format("Node%d", i + 1);
			pdq.CreateNode(name, Node.CEN, QDiscipline.FCFS);
			pdq.SetVisits(name, "Zahtjevi", v[i], S[i]);
		}

		pdq.Solve(Methods.CANON);

		StringJoiner row = new StringJoiner(";");
		row.add(String.format(numFmt, lambda));
		for (int i = 0; i < S.length; i++) {
			String name = String.format("Node%d", i + 1);
			double t = pdq.GetResidenceTime(name, "Zahtjevi", Job.TRANS);
			if (Double.compare(t, time(v[i], S[i], lambda)) != 0) {
				throw new RuntimeException();
			}
		}
		double t = pdq.GetResponse(Job.TRANS, "Zahtjevi");
		if (Double.compare(t, totalTime(lambda, v)) != 0) {
			throw new RuntimeException();
		}
		row.add(String.format(numFmt, t));
		System.out.println(row);
	}


	private static double totalTime(double lambda, double[] v) {
		int n = S.length;
		if (n != v.length) {
			return -1;
		}
		double T = 0.0;
		for (int i = 0; i < S.length; i++) {
			T += time(v[i], S[i], lambda);
		}
		return T;
	}

	private static double time(double v, double S, double lambda) {
		double D = v * S;
		return D / (1.0 - lambda * D);
	}
}
