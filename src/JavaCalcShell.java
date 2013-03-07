/*
 * Java Calculator Shell
 * 
 * (c) Michael Goodyear 2013
 */

import java.util.*; // So we can use our Scanner and NoSuchElementException.
import java.util.regex.*; // so we can use Pattern and Matcher.

public class JavaCalcShell {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		String instruction = "";
		echo_n("calc# ");
		while(true) {
			try {
				instruction = in.nextLine().replaceAll("\\s","");
			} catch(NoSuchElementException e) { return; }
			if(instruction.length() >= 1) {
				if(validateString(instruction)) {
					String result = calculateRecursive(instruction);
					echo("Result: " + result); }
				else { echo("Invalid input."); }
			}
			echo_n("calc# ");
		}
	}

	private static boolean validateString(String strToCheck) {
		
		// Split the string into individual characters.
		char[] chars = strToCheck.toCharArray();
		
		// When we start, we don't know if any of the chars are OK, so okChars = 0.
		int okChars = 0; // Count up rather than set false (less chance of false positive)
		
		// lastCharWasOperator indicates whether the last character tested was an operator
		boolean lastCharWasOperator = false; // Force first char to be numeral.
		boolean lastCharWasCloseParen = false;
		
		for(int i = 0; i < chars.length; i++) {
			boolean isNum = (chars[i] >= 48 && chars[i] <= 57 || chars[i] == 46); // true if number
			boolean isOp = (chars[i] >= 42 && chars[i] <= 47 && chars[i] != 44); // true if operator
			
			// If the last character wasn't an operator, and this one is, and it's not the last or first one, that char is OK
			// This does not allow (for example): "+6", "6+" or "6++6" being used.
			if(!lastCharWasOperator && isOp && i != chars.length-1 && i != 0) { okChars++; }
			
			// Or if the character is an open paren and not last, that char is OK.
			// Also, we'll tell the next loop we just had an operator so we can't have "(+6)".
			else if(chars[i] == 40 && i != chars.length-1) { okChars++; isOp = true; }
			
			// Or if the last character was not an operator and char is a close paren, that's OK
			// For example, this does not allow "(6+)" but does allow "(6+6)".
			else if(!lastCharWasOperator && chars[i] == 41 && i != 0) { okChars++; }
			
			// Or if the char is a number and not after a close paren, that's OK.
			else if(isNum && !lastCharWasCloseParen) { okChars++; }
			
			// Update lastCharWasOperator so it's accurate for the next loop.
			lastCharWasOperator = isOp;
			
			// Update lastCharWasCloseParen similarly.
			lastCharWasCloseParen = (chars[i] == 41);
		}
		
		// If the number of OK chars = number of chars (all chars are OK), return true. Else return false.
		return (okChars == chars.length);
	}
	
	private static String calculateRecursive(String instruction) {
		
		// Parse implicit multiplication i.e. 3(4) becomes 3*(4).
		instruction = instruction.replaceAll("([0-9]+)([(])", "$1*$2");
		
		// Create a new regex matching inner parentheses i.e. (3+4) is matched from 2*(2*(3+4)).
		Pattern p = Pattern.compile("\\([^()]*\\)");
		
		// Create a "matcher" for this pattern
		Matcher m = p.matcher(instruction);
		
		while(m.find()) { // While there are still matches
			
			// Replace the matches with calculated values i.e. 2*(3*(2+3)) becomes 2*(3*5)
			instruction = instruction.replace(m.group(), calculate(m.group().substring(1, m.group().length()-1)));
			
			// Refresh the matcher with the new string - this enables newly created parentheses e.g. (3*5) above to be matched
			m = p.matcher(instruction);
		}
		
		return calculate(instruction); // Finally, calculate the parenthesis-free string.
	}
	
	private static String calculate(String instruction) {
		String[] data = instruction.split("[^0-9.]"); // Create an array of numbers (splitting by operators)...
		String[] code = instruction.split("[0-9.]+"); // ... and corresponding operators (splitting by numbers).
		
		// Perform multiplication and division
		for(int i = 1; i < code.length; i++) { // Loop through the operator array
			int op = code[i].charAt(0); // Find the numerical representation (for our "case" statement)
			Double p1 = Double.parseDouble(data[i-1]); // Find the previous numeral (string to double)
			Double p2 = Double.parseDouble(data[i]); // Find the current numeral (string to double)
			switch(op) {
				case 47: { // If the operator is "/", divide...
					data[i-1] = Double.toString(p1 = p1 / p2); // Replace the previous numeral with the result of the division
					data[i] = "0"; p2 = 0.0; // Replace the current numeral with 0.
					code[i] = "+"; // Replace the current operator with +
					// The result of this is that 4/2 becomes 2+0.
				} break;

				case 42: { // Repeat the same process for "*" (multiply)...
					data[i-1] = Double.toString(p1 = p1 * p2);
					data[i] = "0"; p2 = 0.0;
					code[i] = "+";
				} break;
				
			}
		}
		
		Double num = Double.parseDouble(data[0]); // Set our starting point for addition and subtraction to the first number.
		
		// Perform addition + subtraction
		for(int i = 1; i < code.length; i++) { // Starting at the second number, loop through all remaining numbers.
			int op = code[i].charAt(0); // Find out the operator code.
			Double p2 = Double.parseDouble(data[i]); // Convert the number to a double from its string representation.
			
			switch(op) {
				case 43: { // If the operator is "+"...
					num = num + p2; // ...add the new number to the running total.
				} break;
				
				case 45: { // Or if the operator is "-"...
					num = num - p2; // ...subtract the number from the running total.
				} break;
				
			}
		}
		return num.toString(); // Now return the total as a string.
	}

	private static void echo(String output) { System.out.println(output); } // Quicker println. (You can tell I'm used to PHP.)
	private static void echo_n(String output) { System.out.print(output); } // Quicker print.
	
}
