// 1:1 copy from Microsoft's KB3154518

namespace System.Net
{
	using System.Security.Authentication;
	public static class SecurityProtocolTypeExtensions
	{
		public const SecurityProtocolType Tls12 = (SecurityProtocolType)SslProtocolsExtensions.Tls12;
		public const SecurityProtocolType Tls11 = (SecurityProtocolType)SslProtocolsExtensions.Tls11;
		public const SecurityProtocolType SystemDefault = (SecurityProtocolType)0;
	}
}
