package hr.fer.tel.rassus.pdq;

import com.perfdynamics.pdq.*;

import java.util.StringJoiner;

/**
 * @author Hruntek
 */
public class Homework {

	private static final double LAMBDA_LO = 0.0; // lower
	private static final double LAMBDA_HI = 7.8; // upper
	private static final double LAMBDA_ST = 0.1; // step

	private static final double a = 0.2;
	private static final double b = 0.3;
	private static final double c = 1.0 - a - b;
	private static final double d = 0.3;
	private static final double e = 1.0 - d;
	private static final double f = 0.6;
	private static final double g = 0.2;
	private static final double h = 0.3;

	private static final double[] S = new double[]{0.003, 0.001, 0.01, 0.04, 0.1, 0.13, 0.15};

	public static void main(String[] args) {
		PDQ pdq = new PDQ();

		final boolean totalTimeOnly = true;
		final String numFmt = "%.6f";
		final double fd = 1.0 / (1.0 - f * d);
		final double[] v = new double[S.length];
		v[0] = 1.0;
		v[1] = fd;
		v[2] = fd * (a + b * h) / (1.0 - g * h);
		v[3] = fd * (b + a * g) / (1.0 - g * h);
		v[4] = fd * c;
		v[5] = fd * d;
		v[6] = fd * e;

		StringJoiner header = new StringJoiner(";");
		header.add("Lambda");
		if (!totalTimeOnly) {
			for (int i = 1; i <= S.length; i++) {
				header.add(String.format("Node%d response time", i));
			}
		}
		header.add("Total response time");
		System.out.println(header);

		for (double lambda = LAMBDA_LO; lambda <= LAMBDA_HI; lambda += LAMBDA_ST) {
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
				if (!totalTimeOnly) {
					row.add(String.format(numFmt, t));
				}
			}
			double t = pdq.GetResponse(Job.TRANS, "Zahtjevi");
			if (Double.compare(t, totalTime(lambda, v)) != 0) {
				throw new RuntimeException();
			}
			row.add(String.format(numFmt, t));
			System.out.println(row);
		}
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
