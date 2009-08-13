//  Copyright (C) Christof Meerwald 2003
//  Copyright (C) Dan Watkins 2003
//
//  Use, modification and distribution are subject to the 
//  Boost Software License, Version 1.0. (See accompanying file 
//  LICENSE_1_0.txt or copy at http://www.boost.org/LICENSE_1_0.txt)

//  Digital Mars C++ compiler setup:
#define BOOST_COMPILER __DMC_VERSION_STRING__

#define BOOST_HAS_LONG_LONG
#define BOOST_HAS_PRAGMA_ONCE

#if (__DMC__ <= 0x833)
#define BOOST_FUNCTION_SCOPE_USING_DECLARATION_BREAKS_ADL
#define BOOST_NO_TEMPLATE_TEMPLATES
#define BOOST_NEEDS_TOKEN_PASTING_OP_FOR_TOKENS_JUXTAPOSING
#define BOOST_NO_ARRAY_TYPE_SPECIALIZATIONS
#define BOOST_NO_EXPLICIT_FUNCTION_TEMPLATE_ARGUMENTS
#endif
#if (__DMC__ <= 0x840) || !defined(BOOST_STRICT_CONFIG)
#define BOOST_NO_EXPLICIT_FUNCTION_TEMPLATE_ARGUMENTS
#define BOOST_NO_MEMBER_TEMPLATE_FRIENDS
#define BOOST_NO_OPERATORS_IN_NAMESPACE
#define BOOST_NO_UNREACHABLE_RETURN_DETECTION
#define BOOST_NO_SFINAE
#define BOOST_NO_USING_TEMPLATE
#define BOOST_FUNCTION_SCOPE_USING_DECLARATION_BREAKS_ADL
#define BOOST_NO_INITIALIZER_LISTS
#endif

//
// has macros:
#if (__DMC__ >= 0x840)
#define BOOST_HAS_DIRENT_H
#define BOOST_HAS_STDINT_H
#define BOOST_HAS_WINTHREADS
#endif

#if (__DMC__ >= 0x847)
#define BOOST_HAS_EXPM1
#define BOOST_HAS_LOG1P
#endif

//
// Is this really the best way to detect whether the std lib is in namespace std?
//
#include <cstddef>
#if !defined(__STL_IMPORT_VENDOR_CSTD) && !defined(_STLP_IMPORT_VENDOR_CSTD)
#  define BOOST_NO_STDC_NAMESPACE
#endif


// check for exception handling support:
#ifndef _CPPUNWIND
#  define BOOST_NO_EXCEPTIONS
#endif

#if __DMC__ < 0x800
#error "Compiler not supported or configured - please reconfigure"
#endif
//
// last known and checked version is ...:
#if (__DMC__ > 0x848)
#  if defined(BOOST_ASSERT_CONFIG)
#     error "Unknown compiler version - please run the configure tests and report the results"
#  endif
#endif
