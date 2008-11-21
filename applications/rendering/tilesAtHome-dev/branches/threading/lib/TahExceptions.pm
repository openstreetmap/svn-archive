use strict;

package TahError;

use overload '""' => \&stringify;

sub new
{
    my $class = shift;
    my $self  = {};

    $self = {
        error => undef,
        text => undef,
    };
    bless ($self, $class);

    my ($error, $text) = @_;
    $self->error($error);
    $self->text($text);

    return $self;
}

sub error
{
    my $self = shift();
    my $error = shift();
    if (defined($error))
    {
        $self->{error} = $error;
    }
    return $self->{error};
}

sub text
{
    my $self = shift();
    my $text = shift();
    if (defined($text))
    {
        $self->{text} = $text;
    }
    return $self->{text};
}

sub PROPAGATE
{
    my $self = shift();
    return $self;
}

sub stringify
{
    my $self = shift();
    my $error_string = $self->{error};
    $error_string .= ": " . $self->{text} if ($self->{text});
    return $error_string . "\n";
}

1;
