// a recursive version of the fib.cpp

int fibonacci (int n) {
   if (n < 2) return n;
   else return fibonacci(n-1) + fibonacci(n-2);
}
int main () {
	int answer;
	answer = fibonacci(8);
}
