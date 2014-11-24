#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>

int main()
{
   setuid( 0 );
   system( "/usr/lib/cgi-bin/capabilities/killJava.sh" );

   return 0;
}
