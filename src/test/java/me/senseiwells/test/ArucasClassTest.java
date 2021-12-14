package me.senseiwells.test;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArucasClassTest {
	@Test(timeout = 1000)
	public void testClassSyntax() {
		// TODO: instanceof should work for class types
		
		assertEquals("null", ArucasHelper.runSafeFull(
			"""
			class Testing {
				memberVariable = 'awesome';
				
				fun printContent() {
					print('Omg? ' + this.memberVariable);
					return this;
				}
				
				fun delegate(a, b, c) {
					print('Container: ' + this + ', A: ' + a + ', B: ' + b + ', C: ' + c);
				}
				
				fun delegate() {
					print('...');
				}
				
				/*fun toString() {
					return 'Testing toString';
				}*/
			}
			
			print('');
			x = new Testing();
			print('Class: ' + x);
			x.printContent();
			x.memberVariable = 'This is a different message';
			x.printContent();
			
			print(x);
			print(x.toString());
			//test = x.delegate;
			Q = [ [ 1, 2, 3 ], 'B', 'C' ];
			print(Q);
			print(Q.toString());
			// Call delegate
			//test('input1', 1234, 'testing2');
			//print(test);
			//print('');
			""", ""
		));
	}
}
