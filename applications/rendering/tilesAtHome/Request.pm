# A Request encapsulates a render request.
package Request;

sub new 
{
    my $class = shift;
    my $self = {
        MIN_Z => shift,
        X => shift,
        Y  => shift,
    };
    bless $self, $class;
    return $self;
}

sub Z
{
    my $self = shift;
    my $new_z = shift;
    if ($new_z) {$self->{MIN_Z} = $new_z;}
    return $self->{MIN_Z}
}

sub X
{
    my $self = shift;
    my $new_x = shift;
    if ($new_x) {$self->{X} = $new_x;}
    return $self->{X}
}

sub Y
{
    my $self = shift;
    my $new_y = shift;
    if ($new_y) {$self->{Y} = $new_y;}
    return $self->{Y}
}


true;
