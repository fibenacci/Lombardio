/*
 * Lombardio Source-Available No-Distribution License 1.0
 *
 * Copyright (c) 2026 Benjamin Letzel. All rights reserved.
 *
 * This project is source-available for educational and review purposes only.
 * Redistribution, sublicensing, or commercial use is strictly prohibited.
 *
 * For partnership or cooperation inquiries, please contact the author.
 */

import type { paths as PlatformPaths, components as PlatformComponents } from './platform';
import type { paths as AuctionPaths, components as AuctionComponents } from './auction';
import type { paths as IdentityPaths, components as IdentityComponents } from './identity';
import type { paths as LoanPaths, components as LoanComponents } from './loan';
import type { paths as OnlineAuctionPaths, components as OnlineAuctionComponents } from './online-auction';
import type { paths as PawnTicketPaths, components as PawnTicketComponents } from './pawn-ticket';
import type { paths as ReportingPaths, components as ReportingComponents } from './reporting';

/**
 * Consolidated paths from all microservices.
 */
export interface paths extends 
  PlatformPaths, 
  AuctionPaths, 
  IdentityPaths, 
  LoanPaths, 
  OnlineAuctionPaths, 
  PawnTicketPaths, 
  ReportingPaths {}

/**
 * Consolidated components from all microservices.
 */
export interface components {
  schemas: PlatformComponents['schemas'] & 
           AuctionComponents['schemas'] & 
           IdentityComponents['schemas'] & 
           LoanComponents['schemas'] & 
           OnlineAuctionComponents['schemas'] & 
           PawnTicketComponents['schemas'] & 
           ReportingComponents['schemas'];
}

// Export individual service types for targeted use
export type { PlatformPaths, PlatformComponents };
export type { AuctionPaths, AuctionComponents };
export type { IdentityPaths, IdentityComponents };
export type { LoanPaths, LoanComponents };
export type { OnlineAuctionPaths, OnlineAuctionComponents };
export type { PawnTicketPaths, PawnTicketComponents };
export type { ReportingPaths, ReportingComponents };
