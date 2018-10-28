public class StateAndReward {
	
	private static final int angle_states = 15;
	private static final double angle_max = 1.5;
	
	private static final int vx_states = 6;
	private static final double vx_max = 1.3;

	private static final int vy_states = 8;
	private static final double vy_max = 18.8;

	
	/* State discretization function for the angle controller */
	public static String getStateAngle(double angle, double vx, double vy) {

		int discrete_angle = discretize(angle, angle_states, -angle_max, angle_max); 
		String state = Integer.toString(discrete_angle);
		return state;
	
	}

	/* Reward function for the angle controller */
	public static double getRewardAngle(double angle, double vx, double vy) {
		return 2*(Math.PI - Math.abs(angle));
		
		
		//double reward = 1000/Math.pow(1000, Math.abs(angle));
		//return reward;

	}

	/* State discretization function for the full hover controller */
	public static String getStateHover(double angle, double vx, double vy) {

		
		String angle_state = getStateAngle(angle, vx, vy);
		int disc_vx = discretize(vx, vx_states, -vx_max, vx_max);
		int disc_vy = discretize(vy, vy_states, -vy_max, vy_max);
	
		String state =  "Angle:" +  angle_state +
						"vx:" 	 +  Integer.toString(disc_vx) + 
						"vy:"    +  Integer.toString(disc_vy);
		return state;
	
	}

	/* Reward function for the full hover controller */
	public static double getRewardHover(double angle, double vx, double vy) {	
		
		double hover_reward = 1000/Math.pow(1000, Math.abs(vy));
		double rotation_reward = getRewardAngle(angle, vx, vy);
		double total_reward = rotation_reward * hover_reward;
		
		return total_reward;
	}
	

	// ///////////////////////////////////////////////////////////
	// discretize() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 1 and nrValues-2 is returned.
	//
	// Use discretize2() if you want a discretization method that does
	// not handle values lower than min and higher than max.
	// ///////////////////////////////////////////////////////////
	public static int discretize(double value, int nrValues, double min,
			double max) {
		if (nrValues < 2) {
			return 0;
		}

		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;
			
		return (int) (ratio * (nrValues - 2)) + 1;
	}

	// ///////////////////////////////////////////////////////////
	// discretize2() performs a uniform discretization of the
	// value parameter.
	// It returns an integer between 0 and nrValues-1.
	// The min and max parameters are used to specify the interval
	// for the discretization.
	// If the value is lower than min, 0 is returned
	// If the value is higher than min, nrValues-1 is returned
	// otherwise a value between 0 and nrValues-1 is returned.
	// ///////////////////////////////////////////////////////////
	public static int discretize2(double value, int nrValues, double min,
			double max) {
		double diff = max - min;

		if (value < min) {
			return 0;
		}
		if (value > max) {
			return nrValues - 1;
		}

		double tempValue = value - min;
		double ratio = tempValue / diff;

		return (int) (ratio * nrValues);
	}

}