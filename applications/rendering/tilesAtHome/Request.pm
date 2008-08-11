# A Request encapsulates a render request.
package Request;

#""" Request can be instantiated with (Z,X,Y), alternatively set those with ->ZXY(z,x,y) later."""
# my $r = new Request or my $r = Request->new()
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

# set and/or retrieve the z,x,y of a request
sub ZXY
{
    my $self = shift;
    my ($new_z, $new_x, $new_y) = @_;
    if ($new_z) {$self->{MIN_Z} = $new_z;}
    if ($new_x) {$self->{X} = $new_x;}
    if ($new_y) {$self->{Y} = $new_y;}
    return ($self->{MIN_Z},$self->{X},$self->{Y})
}

# set and/or retrieve the z of a request
sub Z
{
    my $self = shift;
    my $new_z = shift;
    if ($new_z) {$self->{MIN_Z} = $new_z;}
    return $self->{MIN_Z}
}

# set and/or retrieve the x of a request
sub X
{
    my $self = shift;
    my $new_x = shift;
    if ($new_x) {$self->{X} = $new_x;}
    return $self->{X}
}

# set and/or retrieve the y of a request
sub Y
{
    my $self = shift;
    my $new_y = shift;
    if ($new_y) {$self->{Y} = $new_y;}
    return $self->{Y}
}


true;
