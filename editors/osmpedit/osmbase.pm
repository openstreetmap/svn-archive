package osmbase;

use FindBin qw($RealBin);
use lib "$RealBin";
use strict;



sub new {
    my $this = shift;
    my $class = ref($this) || $this;
    my $obj = bless {
	UID => 0,
	KEYVALUE => {},
	@_
    }, $class;
    $obj->add_key_value ("editor", "osmpedit-svn");
    return $obj;
}

sub add_key_value {
    my $self = shift;
    my $key = shift;
    my $value = shift;

    $self->{KEYVALUE}->{$key} = $value;
    if ($key eq "class" and $value eq "motorway") {
	$self->add_key_value ("car", "yes");
    }
    if ($key eq "class" and $value eq "street") {
	$self->add_key_value ("car", "yes");
    }
    if ($key eq "class" and $value eq "path") {
	$self->add_key_value ("foot", "yes");
	$self->add_key_value ("bike", "yes");
    }
}

sub get_key_value {
    my $self = shift;
    my $key = shift;
    return $self->{KEYVALUE}->{$key};
}

sub is_key {
    my $self = shift;
    my $key = shift;
    if (defined $self->{KEYVALUE}->{$key} and 
	$self->{KEYVALUE}->{$key} ne "") {
	return 1;
    } else {
	return 0;
    }
}

sub key_value_hash {
    my $self = shift;
    return $self->{KEYVALUE}
}

sub set_uid {
    my $self = shift;
    my $val = shift;
    $self->{UID} = $val;
}

sub get_uid {
    my $self = shift;
    return $self->{UID};;
}

sub get_tags {
    my $self = shift;
    return $self->{KEYVALUE};
}

sub get_keys {
    my $self = shift;
    return keys %{$self->{KEYVALUE}};
}

sub get_class {
    my $self = shift;
    return $self->{KEYVALUE}->{"class"};
}

sub get_car {
    my $self = shift;
    return $self->{KEYVALUE}->{"car"};
}

sub get_bike {
    my $self = shift;
    return $self->{KEYVALUE}->{"bike"};
}

sub get_foot {
    my $self = shift;
    return $self->{KEYVALUE}->{"foot"};
}

sub get_name {
    my $self = shift;
    return $self->{KEYVALUE}->{"name"};
}



return 1;
