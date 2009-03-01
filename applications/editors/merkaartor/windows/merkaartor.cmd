@Echo off
::
:: Script to run merkaartor through gdb (easily?)
::   (Original from the MythTV project)
::
Echo COMMENTS: --------------------------------------
Echo COMMENTS: This script is used for gathering backtraces using gdb
Echo COMMENTS: --------------------------------------
Echo.
::
:gdbcommands
::
:: Check for and Create if needed the .\gdbcommands.txt 
::
if not exist ./gdbcommands.txt (
    echo handle SIGPIPE nostop noprint > .\gdbcommands.txt
    echo handle SIG33   nostop noprint >> .\gdbcommands.txt
    echo set logging on     >> .\gdbcommands.txt
    echo set pagination off >> .\gdbcommands.txt
::    echo set args -l myth.log --noupnp --nosched --nojobqueue --nohousekeeper --noautoexpire -v all >> .\gdbcommands.txt
    echo run >> .\gdbcommands.txt
    echo thread apply all bt full >> .\gdbcommands.txt
    echo set logging off >> .\gdbcommands.txt )
@Echo off

Echo COMMENTS: --------------------------------------
Echo COMMENTS: Clearing old gdb.txt before running gdb again.
Echo COMMENTS: --------------------------------------
Echo. 
::
:: add current data/time to gdb.txt 
:: will this be a bad idea? who knows? =) 
::
date /t > .\gdb.txt
time /t >> .\gdb.txt

:gdb
::
:: gdb should be in the path. 
::
Echo COMMENTS: --------------------------------------
Echo COMMENTS: If you need to add any switches to merkaartor edit gdbcommands.txt
Echo COMMENTS: --------------------------------------
Echo.
Echo COMMENTS: --------------------------------------
Echo COMMENTS: Starting: gdb
Echo COMMENTS: --------------------------------------
gdb .\merkaartor.exe -batch -q -x .\gdbcommands.txt

Echo.
Echo The backtrace can be found in .\gdb.txt
Echo.
pause
