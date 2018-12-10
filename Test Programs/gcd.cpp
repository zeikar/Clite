
int rem (int x, int y){
   return x - x/y * y;
}

int gcd (int x, int y){
   int z;
   if (y == 0) return x;
   else if (x == 0) return y;
   else {
    z = rem(x, y);
	return gcd(y, z);
   }
}

int main () {
   int answer;
   answer = gcd(24,10);
}
