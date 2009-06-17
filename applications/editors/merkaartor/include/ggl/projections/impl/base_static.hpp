// Generic Geometry Library
//
// Copyright Barend Gehrels 1995-2009, Geodan Holding B.V. Amsterdam, the Netherlands.
// Copyright Bruno Lalande 2008, 2009
// Use, modification and distribution is subject to the Boost Software License,
// Version 1.0. (See accompanying file LICENSE_1_0.txt or copy at
// http://www.boost.org/LICENSE_1_0.txt)

#ifndef GGL_PROJECTIONS_IMPL_BASE_STATIC_HPP
#define GGL_PROJECTIONS_IMPL_BASE_STATIC_HPP

#if defined(_MSC_VER)
// For CRTP, *this is acceptable in constructor -> turn warning off
#pragma warning( disable : 4355 )
#endif // defined(_MSC_VER)


#include <string>

#include <ggl/projections/impl/pj_fwd.hpp>
#include <ggl/projections/impl/pj_inv.hpp>

namespace ggl { namespace projection {








#ifndef DOXYGEN_NO_IMPL
namespace impl
{

// Base-template-forward
template <typename Prj, typename LL, typename XY, typename P>
struct base_t_f
{
public:

    inline base_t_f(const Prj& prj, const P& params)
        : m_par(params), m_prj(prj)
    {}

    inline P params() const {return m_par;}

    inline bool forward(const LL& lp, XY& xy) const
    {
        try
        {
            pj_fwd(m_prj, m_par, lp, xy);
            return true;
        }
        catch(...)
        {
            return false;
        }
    }

    inline std::string name() const
    {
        return this->m_par.name;
    }

protected:

    // Some projections do not work with float -> wrong results
    // TODO: make traits which select <double> from int/float/double and else selects T

    //typedef typename ggl::coordinate_type<LL>::type LL_T;
    //typedef typename ggl::coordinate_type<XY>::type XY_T;
    typedef double LL_T;
    typedef double XY_T;

    P m_par;
    const Prj& m_prj;
};

// Base-template-forward/inverse
template <typename Prj, typename LL, typename XY, typename P>
struct base_t_fi : public base_t_f<Prj, LL, XY, P>
{
public :
    inline base_t_fi(const Prj& prj, const P& params)
        : base_t_f<Prj, LL, XY, P>(prj, params)
    {}

    inline bool inverse(const XY& xy, LL& lp) const
    {
        try
        {
            pj_inv(this->m_prj, this->m_par, xy, lp);
            return true;
        }
        catch(...)
        {
            return false;
        }
    }
};

} // namespace impl
#endif // DOXYGEN_NO_IMPL

}} // namespace ggl::projection

#endif // GGL_PROJECTIONS_IMPL_BASE_STATIC_HPP
