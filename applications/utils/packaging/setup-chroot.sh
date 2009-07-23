#!/bin/bash
# This script is intended to do the setup and chroot environment for 
# Various Distributions on one Machine.
# For this we first do a setup of some chroot systems
#
# For now I started with the debian based ones.

chroot_base_dir="/home/chroot"
username=`grep 1000 /etc/passwd | sed 's/:.*//'`

unset http_proxy
unset HTTP_PROXY
export LANG=C
export DEB_BUILD_OPTIONS="parallel=4"


distri=debian
release=lenny
architecture=32

log_dir="/home/chroot/log-setup"
svn_co_base_dir=${chroot_base_dir}/svn

mkdir -p "${log_dir}"
mkdir -p "${log_dir}/results/"

verbose=true
debug=false
quiet=false
force=false
do_fast=false
do_summarize=false
do_install_dev_tools=false
do_setup_distri=false
do_apt_get_update=false
do_all_distributions=false

# ----------------------------------------------------------------------------------------
function show_error () {
    local logfile="$1"
    local error_text="$2"

    if [ -n "${logfile}" ] ; then
	cat "${logfile}" >>${log_dir}/$chroot/errors.log

	mv "${logfile}" "${logfile}.error"
	logfile="${logfile}.error"
	echo "Logfile: ${logfile}"
	if $verbose ; then
	    echo "tail ${logfile} ... "
	    tail ${logfile}
	fi
    fi

    echo "${BG_WHITE}${RED}!!!!!!! ERROR($chroot): $task_name: $error_text${NORMAL}"
    echo "!!!!!!! ERROR($chroot): $task_name: $error_text" \
	>> ${log_dir}/$chroot/results.log
}
# ------------------------------------------------------------------

for arg in "$@" ; do
    arg_true=true
    arg_false=false
    case $arg in 
	--no-*)
	    arg_true=false
	    arg_false=true
	    arg=${arg/--no-/--}
    esac
    case $arg in
	--all) # do all install/build steps
	    do_setup_distri=$arg_true
	    do_install_dev_tools=$arg_true
	    do_apt_get_update=$arg_true
	    do_debuild_packages=$arg_true
	    ;;


	--setup-distri) # Setup the chroot with the distribution
	    do_setup_distri=$arg_true
	    ;;

	--install-dev-tools) # Install Devtools inside chroot
	    do_install_dev_tools=$arg_true
	    ;;

	--apt-get-update) #  Update debian packages with 'apt-get update;apt-get dist-upgrade'
	    do_apt_get_update=$arg_true
	    ;;

	--iterate-all-distributions) # iterate over all possibilities
	    do_all_distributions=$arg_true
	    ;;

	--distri=*) #	Select distribution [debian|ubuntu|maemo]
	            #	Default: debian
	            #   Where * means iterate over all distributions
	distri=${arg#*=}
	if [ "$distri" = "*" ] ; then
	    do_all_distributions=$arg_true
	fi
	;;
	
	--release=*) #	Specify Release
	             #	debian: etch|lenny|squeeze
	             #	ubuntu: [dapper|feisty|gutsy|hardy|intrepid]
	             #	maemo: [diablo]
	             #	Default: debian squeeze
	             #	If we recognize that it is debian/ubuntu we set this too
	             #	lenny-64 for example also sets the architecture
	             #   Where * means iterate over all distributions
	release=${arg#*=}
	if [ "$release" = "*" ] ; then
	    do_all_distributions=$arg_true
	    continue
	fi
	
	case $release in 
	    *-32)
		architecture=32
		release=${release%-32}
		;;
	    *-i386)
		architecture=32
		release=${release%-i386}
		;;
	    *-64)
		architecture=64
		release=${release%-64}
		;;
	    *-amd64)
		architecture=64
		release=${release%-amd64}
		;;
	esac
	
	if [ -d "/usr/share/debootstrap/scripts/" -a \( ! -s "/usr/share/debootstrap/scripts/${release}" \) ] ; then
	    available_ubuntu_releases=`grep -i 'mirror.*ubuntu' /usr/share/debootstrap/scripts/* | sed 's,.*scripts/,,;s,:.*,,; s,\..*,,'  | sort -u`
	    echo  "Available Ubuntu Release scripts: " $available_ubuntu_releases
	    show_error '' "there is no debootstrap script for $release"
	    exit -1 
	elif grep -q ubuntu /usr/share/debootstrap/scripts/${release}; then
	    distri="ubuntu"
	elif echo "${release}" | grep -q -e diablo; then
	    distri="maemo"
	fi
	;;

	--architecture=*) #Specify Architecture
		     #	Default: 32-bit Linux
	architecture=${arg#*=}
	;;

	--chroot-base-dir=*) # The basedir where to store the chroot environments
	chroot_base_dir=${arg#*=}
	;;
	
	--force) #	force some actions
	    force=$arg_true
	    ;;

	-h)
	    help=$arg_true
	    ;;

	--help)
	    help=$arg_true
	    ;;

	-help)
	    help=$arg_true
	    ;;

	--verbose) #	switch on verbose output
	    verbose=$arg_true
	    quiet=$arg_false
	    ;;

	--debug) #	switch on debugging
	    debug=$arg_true
	    verbose=$arg_true
	    quiet=$arg_false
	    ;;

	--quiet) #	switch on quiet Mode
	    debug=$arg_false
	    verbose=$arg_false
	    quiet=$arg_true
	    ;;

	-debug)
	    debug=$arg_true
	    verbose=$arg_true
	    quiet=""
	    ;;
	--nv) #		be a little bit less verbose
	    verbose=''
	    ;;

	*)
	    echo ""
	    echo "${RED}!!!!!!!!! Unknown option $arg${NORMAL}"
	    echo ""
	    help=true
	    UNKNOWN_OPTION=true
	    ;;
    esac
done # END OF OPTIONS


# define Colors
ESC=`echo -e "\033"`
RED="${ESC}[91m"
GREEN="${ESC}[92m"
YELLOW="${ESC}[93m"
BLUE="${ESC}[94m"
MAGENTA="${ESC}[95m"
CYAN="${ESC}[96m"
WHITE="${ESC}[97m"
BG_RED="${ESC}[41m"
BG_GREEN="${ESC}[42m"
BG_YELLOW="${ESC}[43m"
BG_BLUE="${ESC}[44m"
BG_MAGENTA="${ESC}[45m"
BG_CYAN="${ESC}[46m"
BG_WHITE="${ESC}[47m"
BRIGHT="${ESC}[01m"
UNDERLINE="${ESC}[04m"
BLINK="${ESC}[05m"
REVERSE="${ESC}[07m"
NORMAL="${ESC}[0m"

# ----------------------------------------------------------------------------------------
# HELP
if [ "true" = "$help" ] ; then
    # extract options from case commands above
    options=`grep -E -e esac -e '\s*--.*\).*#' $0 | sed '/esac/,$d;s/.*--/ [--/; s/=\*)/=val]/; s/)[\s ]/]/; s/#.*\s*//; s/[\n/]//g;'`
    options=`for a in $options; do echo -n " $a" ; done`
    echo "$0 $options"
    echo "

    This script does set up some chroot environments.
    This chroot environment then can be used to build 
    debian(for now) based Packages.
    It also installes a basic set of tools inside the 
    chroot environment to later be able to do the packaging.
    "
    # extract options + description from case commands above
    grep -E  \
	-e 'END OF OPTIONS' \
	-e '--.*\).*#' \
	-e '^[\t\s 	]+#' \
	$0 | \
	grep -v /bin/bash | sed '/END OF OPTIONS/,$d;s/.*--/  --/;s/=\*)/=val/;s/)//;s/^[ \t]*#/\t\t/;;s/[ \t]*#/\t/;' 
    echo "Using an option with --no-option-name switches off this option"
    $UNKNOWN_OPTION && exit -1 
    exit;
fi

# ----------------------------------------------------------------------------------------

if ! whoami | grep -q root ; then
    echo "!!!!!! Need to be root"
    exit
fi

if ps fauxwww | grep -v -e grep -e emacs -e "$$" | grep -B 5 -A 2 -e $0 ; then
    echo "There is already another setup_chroot.sh running"
    ps -eo "%t %u %p %C %y %x %c %a " |  (head -n 1 ; grep java)
    exit -1
fi


# ----------------------------------------------------------------------------------------

if $debug ; then
    echo "Started `date`"
else
    #exec 2>&4-
    #exec 2<>/dev/null
    true
fi


# ----------------------------------------------------------------------------------------
function debug_out () {
    task_name="$1"
    description_string="$2"
    LOGFILE="${log_dir}/$chroot/log-$task_name.log"

    if $debug ; then
	echo "DEBUG: -------- $chroot --- '$task_name': Trying '$description_string'"
    fi

    if $debug ; then
	tee $LOGFILE
	cat "${LOGFILE}" >>${log_dir}/$chroot/full-debug.log
    else
	cat >$LOGFILE
    fi
    rc=`tail -1 | grep "EXIT CODE" $LOGFILE`
    rc=${rc##EXIT CODE=}
    if [ "$rc" != "0" ] ; then    
	echo "$task_name: Trying $description_string" >>${log_dir}/$chroot/errors.log
	cat "${LOGFILE}" >>${log_dir}/$chroot/errors.log
	show_error "${LOGFILE}" "cannot do '$description_string' Exit Code $rc" 
	return -1
    fi

    $debug && echo "DEBUG: $LOGFILE"
    return $rc
}

function set_rc {
    rc=$?
    echo "EXIT CODE=$rc"
}

# ----------------------------------------------------------------------------------------

prerequisites=true
debootstrap --version >/dev/null || prerequisites=false
dchroot --version >/dev/null || prerequisites=false
rpm --version >/dev/null || prerequisites=false
if ! $prerequisites; then
    $quiet || echo "-- $chroot ----- install debootstrap,dchroot,rpm on host"
    task_name="aptitide_install_debootstrap_dchroot"
    aptitude --assume-yes install debootstrap dchroot rpm
    if [ "$?" -ne "0" ] ; then
	show_error "${LOGFILE}" "cannot install debootstrap dchroot rmp"
	exit -1
    fi
fi


# ----------------------------------------------------------------------------------------
#                                         Functions
# ----------------------------------------------------------------------------------------


# ----------------------------------------------------------------------------------------
function setup_variables {
    $debug && echo ""

    chroot="$distri-$release-$architecture"
    chroot_dir="$chroot_base_dir/$chroot"


    if $verbose || $debug ; then
	echo
	echo "Distribution: '$distri'"
	echo "Release: '$release'"
	echo "Architecture: '$architecture'"
    fi

    variant=" --variant=buildd "

    # --------------------------------------------
    # 32/64 -Bit
    case $architecture in
	linux32|32)
	    arch="  --arch i386"
	    personality="linux32"
	    ;;
	amd64|64)
	    arch="  --arch amd64"
	    personality="linux"
	    ;;
	arm)
	    arch="  --arch arm"
	    personality="linux"
	    ;;
	*)
	    show_error "${LOGFILE}" "Uknown Architecture $architecture"
	    return -1
    esac

    # --------------------------------------------
    #  debian/ubuntu/maemo//Suse
    debian_based=false
    suse_based=false
    case $distri in
	debian)
	    archive_url="http://ftp.de.debian.org/debian"
	    debian_based=true
	    case $release in
		etch|lenny|squeeze)
		    $debug && echo "Specified Release '$release' is OK"
		    ;;
		*)
		    show_error "${LOGFILE}" "unknown Debian Release $release"
		    return -1
		    ;;
	    esac
	    ;;
	ubuntu)
	    archive_url="http://archive.ubuntu.com/ubuntu/"
	    debian_based=true
	    case $release in
		dapper|feisty|gutsy|hardy|intrepid)
		    $debug && echo "Specified Release '$release' is OK"
		    ;;
		*)
		    show_error "${LOGFILE}" "unknown Ubuntu Release $release"
		    return -1
		    ;;
	    esac
	    ;;
	maemo)
	    archive_url="http://archive.maemo.com/"
	    debian_based=true
	    case $release in
		diablo)
		    $debug && echo "Specified Release '$release' is OK"
		    ;;
		*)
		    show_error "${LOGFILE}" "unknown Ubuntu Release $release"
		    return -1
		    ;;
	    esac
	    ;;
	suse)
	    archive_url="http://download.opensuse.org/distribution/11.1/repo/oss/"
	    suse_based=true
	    case $release in
		11.1)
		    $debug && echo "Installing Suse"
		    ;;
	    esac
	    ;;
	*)
	    show_error "${LOGFILE}" "Uknown Distribution $distri"
	    return -1
    esac
    

    return 0
}

# ----------------------------------------------------------------------------------------
function setup_distri {

    $do_setup_distri ||    return 0 

    $debug && echo ""
    # --------------------------------------------
    if $debian_based ; then
	if  $force || [ ! -s "$chroot_dir/etc/debian_version" ]; then
	    $quiet || echo "-- $chroot -- debootstrap ----------------------------------------"
	    command="debootstrap $variant $arch $release $chroot_dir $archive_url"
	    $debug && echo $command
	    task_name="debootstrap"
	    { debootstrap $variant $arch "$release" "$chroot_dir" "$archive_url" \
		2>&1 ;set_rc; } | debug_out "debootstrap" "$command"
	    test "$?" -ne "2" && return -1
	fi
    fi

    if $suse_based ; then
	# $archive_url/bla.rpm
	rpm --root $chroot_dir -ihv `cat /tmp/rpms-suse`
	echo "$chroot not implemented .... Ends here for now ..." \
	    | tee -a ${log_dir}/$chroot/results.log
	return -1
    fi

    # --------------------------------------------
    if true; then
	$quiet || echo "-- $chroot -- mount /proc, ... ----------------------------------------"

	# Google: ...
	# mount --bind /dev /path-to-your-chroot/dev
	# mount --bind /dev/pts /path-to-your-chroot/dev/pts
	# mount --bind /dev/shm /path-to-your-chroot/dev/shm
	# mount -t proc none /path-to-your-chroot/proc
	# mount -t sysfs none /path-to-your-chroot/sys

	for dev_type  in proc; do 
            # mount -o bind /proc  $chroot_dir/proc
	    if ! grep -q -e "/proc.*$chroot_dir/proc proc bind" /etc/fstab; then
		echo "/proc  $chroot_dir/proc proc bind" >>/etc/fstab
	    fi
	    umount "$chroot_dir/proc"
	    mount "$chroot_dir/proc"
	    if $debug ; then
		echo "mounted:"
		mount | grep -e '^/proc'
	    fi
	done
    fi

    # For DNS Lookup
    cp /etc/resolv.conf $chroot_dir/etc/resolv.conf


    $quiet || echo "-- $chroot -- Update Sources.list----------------------------------------"
    if [ ! -s  "$chroot_dir/etc/apt/sources.list.DIST" ] ; then
	mv "$chroot_dir/etc/apt/sources.list" "$chroot_dir/etc/apt/sources.list.DIST"
    fi
    case $distri in
	ubuntu)
	    cat <<EOF >$chroot_dir/etc/apt/sources.list
deb http://archive.ubuntu.com/ubuntu/ $release main restricted multiverse universe
deb http://de.archive.ubuntu.com/ubuntu $release main restricted
deb-src http://de.archive.ubuntu.com/ubuntu $release main restricted
deb http://security.ubuntu.com/ubuntu/ $release-security restricted main multiverse universe

deb http://archive.ubuntu.com/ubuntu/ $release-updates restricted main multiverse universe
deb http://archive.ubuntu.com/ubuntu/ $release universe
EOF
	    ;;

	debian)
	    cat >$chroot_dir/etc/apt/sources.list <<EOF
deb http://ftp.de.debian.org/debian/ $release main contrib non-free
deb-src http://ftp.de.debian.org/debian/ $release main contrib non-free

deb http://security.debian.org/ $release/updates main contrib non-free
deb-src http://security.debian.org/ $release/updates main contrib non-free
EOF

	    case $release in
		etch)
		    $quiet || echo "deb http://www.backports.org/debian $release-backports main contrib non-free" \
                        >>$chroot_dir/etc/apt/sources.list
#		    echo "deb http://volatile.debian.org/debian-volatile $release/volatile main contrib non-free" \
#                         >>$chroot_dir/etc/apt/sources.list
		    ;;
	    esac

	    case $architecture in
		linux32|32)
		    cat >>$chroot_dir/etc/apt/sources.list <<EOF

#deb     http://www.gpsdrive.de/debian testing main
#deb-src http://www.gpsdrive.de/debian testing main
EOF
		    ;;
	    esac
	    ;;
    esac

    # ----------------------------------------------------------------------------------------
    if $do_apt_get_update; then
	$debug && echo ""
	command="apt-get update"
	$quiet || echo "-- $chroot ---------- Update to newest version (update;dist-upgrade)"
	$debug && echo "---- apt-get update"
	task_name="apt-get_update"
	{ chroot $chroot_dir apt-get --quiet --force-yes update \
	    2>&1  ;set_rc; } | debug_out "$task_name" "$command"
	test "$?" -ne "0" && return -1
	
	$debug && echo "---- dist-upgrade"
	command="dist-upgrade"
	task_name="apt-get_dist-upgrade"
	{ chroot $chroot_dir apt-get --quiet --assume-yes --force-yes dist-upgrade \
	    2>&1  ;set_rc; } | debug_out $task_name "$command"
	test "$?" -ne "0" && return -1
	
	case $distri in
	    ubuntu)
		$debug && echo "---- apt-get install gnupg"
		command="apt-get install aptitude"
		task_name="apt-get_install_gpg"
		{ chroot $chroot_dir apt-get --assume-yes --force-yes install gnupg \
		    2>&1  ;set_rc; } | debug_out $task_name "$command"
		test "$?" -ne "0" && return -1
    		;;
	esac

	$debug && echo "---- apt-get install aptitude"
	command="apt-get install aptitude"
	task_name="apt-get_install_aptitude"
	if ! chroot $chroot_dir aptitude --version >/dev/null ; then
	    { chroot $chroot_dir apt-get --quiet --assume-yes --force-yes install aptitude \
		2>1   ;set_rc; } | debug_out $task_name "$command"
	    test "$?" -ne "0" && return -1
	fi

    fi

    # ----------------------------------------------------------------------------------------
    chroot $chroot_dir ls -ld /home/$username | grep "$username root" >/dev/null 2>/dev/null
    user_dir_ok=$?
    id $username >/dev/null
    user_id_ok=$?
    if [ ! $force -a "$user_id_ok" -eq 0 -a "$user_dir_ok" -eq 0 ] ; then
	$debug && echo "--- $chroot -- User Setup OK"
    else
	$debug && echo ""
	echo "--- $chroot -- Add User"
	chroot $chroot_dir useradd $username
	if [ "$?" -ne "0" ] ; then
	    echo "WARNING: cannot add user $username"
	fi
	chroot $chroot_dir mkdir -p /home/$username
	if [ "$?" -ne "0" ] ; then
	    show_error "${LOGFILE}" "cannot create homedir for $username"
	    return -1
	fi

	# Show Chroot Type in bash Prompt
	echo '#!/bin/bash' > $chroot_dir/home/$username/.profile 
	echo 'PS1="`version`:\u@\h:\w\$"' >>$chroot_dir/home/$username/.profile 

	# set Permissions for user home
	chroot $chroot_dir chown -R $username /home/$username
	if [ "$?" -ne "0" ] ; then
	    show_error "${LOGFILE}" "cannot grant user $username access to its home"
	    return -1
	fi

    fi


    # ----------------------------------------------------------------------------------------
    if ! grep -q -e "$chroot " /etc/dchroot.conf; then
	$debug && echo ""
	$quiet || echo "-- $chroot ---------- dchroot.conf"
	$quiet || echo "-- $chroot ---------- add to dchroot.conf"
	echo "$chroot $chroot_dir ${personality}" >>/etc/dchroot.conf
    fi

    if ! sudo -u $username dchroot -c $chroot "id $username" >/dev/null 2>&1 ; then
	sudo -u $username dchroot -c $chroot "id $username"
	show_error "${LOGFILE}" "calling dchroot"
	return -1  
    fi


    # ----------------------------------------------------------------------------------------
    # Add a version command inside the chroot
    echo "#!/bin/bash"  >$chroot_dir/bin/version
    echo "echo \"$chroot\"" >>$chroot_dir/bin/version
    chmod a+rx $chroot_dir/bin/version

    return 0
}

# ----------------------------------------------------------------------------------------
function install_devtools {
    $do_install_dev_tools || return 0

    $debug && echo ""
    $quiet || echo "-- $chroot -------------------- Install development Tools"

    development_tools="pbuilder subversion wget gnupg nano subversion vim cmake less"
    development_tools="$development_tools devscripts debconf debhelper"
    development_tools="$development_tools debian-archive-keyring debian-edu-archive-keyring debian-keyring"
    development_tools="$development_tools sun-java6-jdk openssh-server apt-utils dpatch"
    case $distri in
	debian)
	    development_tools="$development_tools python-central"
	    ;;
	ubuntu)
	    development_tools="$development_tools ubuntu-keyring python-central"
	    ;;
    esac

    $debug && echo "Check For Old dependencies: 'apt-get -f  --quiet --force-yes --assume-yes install'"
    task_name="apt-get_-f_install"
    command="Install development tools: apt-get -f install"
    { chroot $chroot_dir apt-get -f install \
	2>&1  ;set_rc; } | debug_out $task_name "$command"
    test "$?" -ne "0" && return -1

    $debug && echo "Check For Packages: '$development_tools'"
    task_name="aptitude_install_developmenttools"
    command="Install development tools: aptitude --assume-yes install $development_tools"
    { chroot $chroot_dir aptitude --assume-yes install $development_tools \
	2>&1  ;set_rc; } | debug_out $task_name "$command"
    test "$?" -ne "0" && return -1
    return 0
}


# ----------------------------------------------------------------------------------------
#                                          Main
# ----------------------------------------------------------------------------------------
function main_func {

    setup_variables || return -1


    mkdir -p ${log_dir}-old
    old_log=${log_dir}-old/$chroot
    echo $old_log
    rm -rf  ${log_dir}-old/$chroot
    test -d ${log_dir}/$chroot && mv -f ${log_dir}/$chroot ${log_dir}-old/
    mkdir -p ${log_dir}/$chroot

    setup_distri || return -1 
    install_devtools || return -1 

    echo "Usage of chroot $chroot with"
    echo "        dchroot -c $chroot"
    return 0
}


# ----------------------------------------------------------------------------------------

if $do_all_distributions ; then
    
    for architecture in 64 32 ; do 
	
	$quiet || echo "# -------------------- Debian"
	distri=debian
	for release in squeeze lenny ; do  
	    main_func || echo "Incomplete $distri-$release-$architecture"
	done

	$quiet || echo "# -------------------- Ubuntu"
	distri=ubuntu
        #for release in dapper feisty  gutsy hardy intrepid jaunty; do
	for release in intrepid hardy ; do
	#for release in intrepid ; do
	    main_func  || echo "Incomplete $distri-$release-$architecture"
	done

	if false; then # TODO: Not YET Implemented
	    echo "# SuSe"
	    distri=suse
	    for release in 11.1 11.0 10.3 10.2 ; do
		main_func || echo "Incomplete $distri-$release-$architecture"
	    done 
	fi
    done
    
else
    main_func || echo "Incomplete $distri-$release-$architecture"
fi


if $debug ; then
    echo "Finished `date`"
fi

