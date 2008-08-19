# A Config class for t@h.
#
# Copyright 2008, by Matthias Julius
# licensed under the GPL v2 or (at your option) any later version.

package TahConf;

use strict;
use AppConfig qw(:argcount);

my $instance = undef; # Singleton instance of Config class

sub getConfig 
{
    my $class = shift;
    my $self;
    if (defined($instance))
    {
        $self = $instance;
    }
    else
    {
        $self = {
            Config => undef       # AppConfig object
        };
        bless $self, $class;
        my $Config = AppConfig->new({CREATE => 1,                            # Autocreate unknown config variables
                                     GLOBAL => {DEFAULT  => undef,           # Create undefined Variables by default
                                                ARGCOUNT => ARGCOUNT_ONE}}); # Simple Values (no arrays, no hashmaps)
        $Config->define("help|usage!");
        $Config->define("nodownload=s");
        $Config->set("nodownload",0);
        $Config->file("config.defaults", "layers.conf", "tilesAtHome.conf", "authentication.conf");
        $Config->args();                # overwrite config options with command line options
        $Config->file("general.conf");  # overwrite with hardcoded values that must not be changed

        $self->{Config} = $Config;
        $instance = $self;
    }

    return $self;
}


#-----------------------------------------------------------------------------
# retrieve a config setting
#-----------------------------------------------------------------------------
sub get
{
    my $self = shift();
    return $self->{Config}->get(@_);
}


#-----------------------------------------------------------------------------
# set a config setting
#-----------------------------------------------------------------------------
sub set
{
    my $self = shift();
    return $self->{Config}->set(@_);
}
1;
